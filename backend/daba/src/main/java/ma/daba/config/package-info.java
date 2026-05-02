/** Infrastructure adapters (security, caches, outbound HTTP proxies). */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"common", "identity"}
)
package ma.daba.config;
