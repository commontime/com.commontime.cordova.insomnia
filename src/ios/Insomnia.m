/*
  Copyright 2013-2017 appPlant GmbH

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

#import "APPMethodMagic.h"
#import "Insomnia.h"
#import <Cordova/CDVAvailability.h>
#import "LSApplicationWorkspace.h"
#include "notify.h"

@implementation Insomnia

#pragma mark -
#pragma mark Constants

NSString* const kAPPBackgroundJsNamespace = @"window.plugins.insomnia";
NSString* const kAPPBackgroundEventActivate = @"activate";
NSString* const kAPPBackgroundEventDeactivate = @"deactivate";


#pragma mark -
#pragma mark Life Cycle

/**
 * Called by runtime once the Class has been loaded.
 * Exchange method implementations to hook into their execution.
 */
+ (void) load
{
    [self swizzleWKWebViewEngine];
}

/**
 * Initialize the plugin.
 */
- (void) pluginInitialize
{
    [self setup];
    [self registerAppforDetectLockState];
    [self configureAudioPlayer];
    [self configureAudioSession];
    [self observeLifeCycle];
}

- (void) setup
{
    enabled = NO;
    BOOL autoStart = [[[NSBundle mainBundle] objectForInfoDictionaryKey:@"InsomniaAutoStart"] boolValue];
    if (autoStart) {
        enabled = YES;
    }
    inBackground = NO;
    foregroundAfterUnlock = NO;
}

/**
 * Register the listener for pause and resume events.
 */
- (void) observeLifeCycle
{
    NSNotificationCenter* listener = [NSNotificationCenter
                                      defaultCenter];

        [listener addObserver:self
                     selector:@selector(keepAwake)
                         name:UIApplicationDidEnterBackgroundNotification
                       object:nil];

        [listener addObserver:self
                     selector:@selector(stopKeepingAwake)
                         name:UIApplicationWillEnterForegroundNotification
                       object:nil];

        [listener addObserver:self
                     selector:@selector(handleAudioSessionInterruption:)
                         name:AVAudioSessionInterruptionNotification
                       object:nil];
    
        [listener addObserver:self
                     selector:@selector(handleCTAudioPlay:)
                         name:@"CTIAudioPlay"
                       object:nil];
    
        [listener addObserver:self
                     selector:@selector(handleCTAudioFinished:)
                         name:@"CTIAudioFinished"
                       object:nil];
}

#pragma mark -
#pragma mark Interface

/**
 * Enable the mode to stay awake
 * when switching to background for the next time.
 */
- (void) enable:(CDVInvokedUrlCommand*)command
{
    if (enabled)
    {
        [self execCallback:command];
        return;
    }

    enabled = YES;
    
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (state == UIApplicationStateBackground || state == UIApplicationStateInactive)
    {
        [self keepAwake];
    }
    
    [self execCallback:command];
}

/**
 * Disable the background mode
 * and stop being active in background.
 */
- (void) disable:(CDVInvokedUrlCommand*)command
{
    if (!enabled)
    {
        [self execCallback:command];
        return;
    }

    enabled = NO;
    [self stopKeepingAwake];
    [self execCallback:command];
}

/**
 * Disable the background mode
 * and stop being active in background.
 */
- (void) switchOnScreenAndForeground:(CDVInvokedUrlCommand*)command
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        PrivateApi_LSApplicationWorkspace* workspace;
        workspace = [NSClassFromString(@"LSApplicationWorkspace") new];
        NSString *bundleId = [[NSBundle mainBundle] bundleIdentifier];
        BOOL isOpen = [workspace openApplicationWithBundleID:bundleId];
        if (!isOpen) {
            // Reason for failing to open up the app is almost certainly because the phone is locked.
            // Therefore set the flag to bring to the front after unlock to true.
            foregroundAfterUnlock = YES;
        }
    });
    
    [self execCallback:command];
}

#pragma mark -
#pragma mark Core

/**
 * Keep the app awake.
 */
- (void) keepAwake
{
    if (!enabled)
        return;
    
    [self configureAudioSession];

    [audioPlayer play];
    [self fireEvent:kAPPBackgroundEventActivate];
    
    inBackground = YES;
}

/**
 * Let the app going to sleep. 
 */
- (void) stopKeepingAwake
{
    if (TARGET_IPHONE_SIMULATOR) {
        NSLog(@"BackgroundMode: On simulator apps never pause in background!");
    }

    if (audioPlayer.isPlaying) {
        [self fireEvent:kAPPBackgroundEventDeactivate];
    }

    [audioPlayer pause];
    
    inBackground = NO;
}

/**
 * Listen for device lock/unlock.
 */
- (void)registerAppforDetectLockState {
    int notify_token;
    notify_register_dispatch("com.apple.springboard.lockstate", &notify_token,dispatch_get_main_queue(), ^(int token) {
        uint64_t state = UINT64_MAX;
        notify_get_state(token, &state);
        if(state == 0) {
            if (foregroundAfterUnlock) {
                [self switchOnScreenAndForeground:nil];
                foregroundAfterUnlock = NO;
            }
        }
    });
}

/**
 * Configure the audio player.
 */
- (void) configureAudioPlayer
{
    NSString* path = [[NSBundle mainBundle]
                      pathForResource:@"appbeep" ofType:@"m4a"];

    NSURL* url = [NSURL fileURLWithPath:path];


    audioPlayer = [[AVAudioPlayer alloc]
                   initWithContentsOfURL:url error:NULL];

    audioPlayer.volume        = 0;
    audioPlayer.numberOfLoops = -1;
};

/**
 * Configure the audio session.
 */
- (void) configureAudioSession
{
    AVAudioSession* session = [AVAudioSession
                               sharedInstance];

    // Don't activate the audio session yet
    [session setActive:NO error:NULL];

    // Play music even in background and dont stop playing music
    // even another app starts playing sound
    [session setCategory:AVAudioSessionCategoryPlayback
             withOptions:AVAudioSessionCategoryOptionMixWithOthers
                   error:NULL];

    // Active the audio session
    [session setActive:YES error:NULL];
};

#pragma mark -
#pragma mark Helper

/**
 * Simply invokes the callback without any parameter.
 */
- (void) execCallback:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult *result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK];

    [self.commandDelegate sendPluginResult:result
                                callbackId:command.callbackId];
}

/**
 * Restart playing sound when interrupted by phone calls.
 */
- (void) handleAudioSessionInterruption:(NSNotification*)notification
{
    [self fireEvent:kAPPBackgroundEventDeactivate];
    [self keepAwake];
}

/**
 * Stop background audio correctly if the app itself is about to play audio.
 */
- (void) handleCTAudioPlay:(NSNotification*)notification
{
    if (!enabled)
        return;
    
    [audioPlayer stop];
    [[AVAudioSession sharedInstance] setActive:NO error:nil];
}

/**
 * App has stopped playing audio so start background audio.
 */
- (void) handleCTAudioFinished:(NSNotification*)notification
{
    if (!enabled)
        return;
    
    if (!inBackground)
        return;
    
    [self configureAudioPlayer];
    [self configureAudioSession];
    [audioPlayer play];
}

/**
 * Find out if the app runs inside the webkit powered webview.
 */
+ (BOOL) isRunningWebKit
{
    return IsAtLeastiOSVersion(@"8.0") && NSClassFromString(@"CDVWKWebViewEngine");
}

/**
 * Method to fire an event with some parameters in the browser.
 */
- (void) fireEvent:(NSString*)event
{
    NSString* active =
    [event isEqualToString:kAPPBackgroundEventActivate] ? @"true" : @"false";

    NSString* flag = [NSString stringWithFormat:@"%@._isActive=%@;",
                      kAPPBackgroundJsNamespace, active];

    NSString* depFn = [NSString stringWithFormat:@"%@.on%@();",
                       kAPPBackgroundJsNamespace, event];

    NSString* fn = [NSString stringWithFormat:@"%@.fireEvent('%@');",
                    kAPPBackgroundJsNamespace, event];

    NSString* js = [NSString stringWithFormat:@"%@%@%@", flag, depFn, fn];

    [self.commandDelegate evalJs:js];
}

#pragma mark -
#pragma mark Swizzling

/**
 * Method to swizzle.
 */
+ (NSString*) wkProperty
{
    NSString* str = @"YWx3YXlzUnVuc0F0Rm9yZWdyb3VuZFByaW9yaXR5";
    NSData* data  = [[NSData alloc] initWithBase64EncodedString:str options:0];

    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

/**
 * Swizzle some implementations of CDVWKWebViewEngine.
 */
+ (void) swizzleWKWebViewEngine
{
    if (![self isRunningWebKit])
        return;

    Class wkWebViewEngineCls = NSClassFromString(@"CDVWKWebViewEngine");
    SEL selector = NSSelectorFromString(@"createConfigurationFromSettings:");

    SwizzleSelectorWithBlock_Begin(wkWebViewEngineCls, selector)
    ^(CDVPlugin *self, NSDictionary *settings) {
        id obj = ((id (*)(id, SEL, NSDictionary*))_imp)(self, _cmd, settings);

        [obj setValue:[NSNumber numberWithBool:YES]
               forKey:[Insomnia wkProperty]];

        return obj;
    }
    SwizzleSelectorWithBlock_End;
}

@end
