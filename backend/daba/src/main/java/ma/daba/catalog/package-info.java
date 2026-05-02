/**
 * Catalog Module — service categories and artisan profiles.
 *
 * <p><b>Owns:</b> ServiceCategory, ArtisanProfile entities.
 * <p><b>Depends on:</b> identity (for user data).
 * <p><b>Must NOT:</b> import from matching or booking internals.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "identity"
)
package ma.daba.catalog;
