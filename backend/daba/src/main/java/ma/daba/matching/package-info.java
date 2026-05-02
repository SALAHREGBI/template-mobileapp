/**
 * Matching Module — spatial artisan discovery and Waterfall Ping algorithm.
 *
 * <p><b>Owns:</b> ArtisanGeodata entity, MatchmakingService, Redis geo-index.
 * <p><b>Depends on:</b> identity, catalog.
 * <p><b>Must NOT:</b> import from booking internals.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"identity", "catalog"}
)
package ma.daba.matching;
