package ma.daba.identity;

import ma.daba.identity.dto.UserSummaryResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates Bearer-authenticated routing — reuse this pattern for real domain resources.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class CurrentUserController {

    @GetMapping("/me")
    UserSummaryResponse me(@AuthenticationPrincipal User user) {
        return UserSummaryResponse.from(user);
    }
}
