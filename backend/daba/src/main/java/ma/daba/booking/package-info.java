/**
 * Booking Module — transactional booking lifecycle, state machine, and feedback.
 *
 * <p><b>Owns:</b> Booking entity, BookingService, Review entity, FeedbackService.
 * <p><b>Depends on:</b> identity, catalog, matching.
 * <p><b>Publishes:</b> BookingCompletedEvent (consumed by feedback pipeline).
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"identity", "catalog", "matching"}
)
package ma.daba.booking;
