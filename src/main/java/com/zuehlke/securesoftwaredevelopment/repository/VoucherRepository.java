package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Voucher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VoucherRepository {

    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(VoucherRepository.class);

    private final JdbcTemplate jdbc;

    public VoucherRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @throws org.springframework.dao.DuplicateKeyException if a voucher with this code already exists
     */
    public void create(String code, int discount) {
        jdbc.update("INSERT INTO voucher(code, discount) VALUES (?, ?)", code, discount);
        AUDIT_LOGGER.audit("Created voucher with " + discount + "% discount");
    }

    public List<Voucher> getAll() {
        return jdbc.query("SELECT id, code, discount FROM voucher ORDER BY id",
                (rs, rowNum) -> new Voucher(rs.getInt("id"), rs.getString("code"), rs.getInt("discount")));
    }

    /**
     * Consumes a voucher. A single DELETE both checks and spends it, so two concurrent
     * purchases can never redeem the same code twice.
     *
     * @return true if the voucher existed and is now used up
     */
    public boolean redeem(String code) {
        boolean redeemed = jdbc.update("DELETE FROM voucher WHERE code = ?", code) == 1;
        if (redeemed) {
            AUDIT_LOGGER.audit("Redeemed a voucher");
        }
        return redeemed;
    }
}
