package roomescape.repository;

import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimes;

@Repository
public class JdbcTemplateReservationTimeRepository implements ReservationTimeRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) ->
            new ReservationTime(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime()
            );

    public JdbcTemplateReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        save(reservationTime, keyHolder);
        return new ReservationTime(keyHolder.getKey().longValue(), reservationTime.getStartAt());
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT id FROM RESERVATION_TIME WHERE start_at = ?)",
                Boolean.class, startAt);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        List<ReservationTime> times = jdbcTemplate.query(
                "SELECT * FROM RESERVATION_TIME WHERE id = ?",
                RESERVATION_TIME_ROW_MAPPER, id);
        return times.stream().findFirst();
    }

    @Override
    public ReservationTimes findAll() {
        List<ReservationTime> findReservationTimes = jdbcTemplate.query(
                "SELECT * FROM RESERVATION_TIME", RESERVATION_TIME_ROW_MAPPER);
        return new ReservationTimes(findReservationTimes);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM RESERVATION_TIME WHERE id = ?", id);
    }

    private void save(ReservationTime reservationTime, KeyHolder keyHolder) {
        jdbcTemplate.update(con -> {
            PreparedStatement pstmt = con.prepareStatement(
                    "INSERT INTO RESERVATION_TIME(start_at) VALUES ( ? )",
                    new String[]{"id"});
            pstmt.setTime(1, Time.valueOf(reservationTime.getStartAt()));
            return pstmt;
        }, keyHolder);
    }
}
