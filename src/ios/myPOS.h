#import <Cordova/CDV.h>

@interface myPOS : CDVPlugin

- (void) payment:(CDVInvokedUrlCommand*)command;

@end