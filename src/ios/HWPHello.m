#import "hello.h" 

@implementation Hello 

- (void)greet:(NSMutableArray*)args withDict:(NSMutableDictionary*)opts
{
    NSString* callbackId = [args objectAtIndex:0];
    
	NSString* name = [args objectAtIndex:1];
	NSString* msg = [NSString stringWithFormat: @"Hello, %@", name];
    
	CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:msg];
    
	[super success:result callbackId:callbackId];
}

@end