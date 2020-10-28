#import "HoneywellPlugin.h"
#if __has_include(<honeywell_plugin/honeywell_plugin-Swift.h>)
#import <honeywell_plugin/honeywell_plugin-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "honeywell_plugin-Swift.h"
#endif

@implementation HoneywellPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftHoneywellPlugin registerWithRegistrar:registrar];
}
@end
