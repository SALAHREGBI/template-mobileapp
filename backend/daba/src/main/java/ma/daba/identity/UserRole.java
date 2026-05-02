package ma.daba.identity;

/**
 * Platform roles. Each role corresponds to a distinct app experience:
 * <ul>
 *   <li>{@code CLIENT}  — discovers and books services</li>
 *   <li>{@code ARTISAN} — receives and fulfils bookings</li>
 *   <li>{@code ADMIN}   — moderation, compliance, analytics</li>
 * </ul>
 */
public enum UserRole {
    CLIENT,
    ARTISAN,
    ADMIN
}
