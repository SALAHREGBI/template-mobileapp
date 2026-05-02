package ma.daba.common;

import org.springframework.modulith.ApplicationModule;

/**
 * Anchor for Spring Modulith: declares {@code ma.daba.common} as an OPEN module ArchUnit can reflect.
 */
@ApplicationModule(displayName = "Common", type = ApplicationModule.Type.OPEN)
final class CommonModulith {}
