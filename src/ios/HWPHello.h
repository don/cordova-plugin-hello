#import <Cordova/CDVPlugin.h>

@interface HWPHello : CDVPlugin

- (void) greet:(NSMutableArray*)arguments withDict:(NSMutableDictionary*)options;

@end