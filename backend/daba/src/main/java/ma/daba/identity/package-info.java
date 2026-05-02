/**
 * Identity Module — user authentication, registration, JWT issuance.
 *
 * <p><b>Owns:</b> User entity, role management, auth tokens.
 * <p><b>Exposes:</b> {@link ma.daba.identity.UserService} for cross-module queries.
 * <p><b>Must NOT:</b> import from booking, matching, or catalog internals.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"common"}
)
package ma.daba.identity;
