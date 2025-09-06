package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.Tag;
import com.zuehlke.securesoftwaredevelopment.domain.Voucher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VoucherRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VoucherRepository.class);
    private static final AuditLogger AUDIT_LOGGER = AuditLogger.getAuditLogger(VoucherRepository.class);

    private DataSource dataSource;

    public VoucherRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(int userId, String code, int value) {
        if (code == null || code.trim().isEmpty()) {
            return;
        }
        
        String query = "INSERT INTO voucher(code, value) VALUES(?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setString(1, code);
            statement.setInt(2, value);
            statement.executeUpdate();
            
            AUDIT_LOGGER.audit("Successfully created voucher with code '" + code + "' and value " + value);
        } catch (SQLException e) {
            LOG.warn("Failed to create voucher with code {}", code, e);
        }
    }

    public boolean checkIfVoucherExist(String voucher) {
        if (voucher == null || voucher.trim().isEmpty()) {
            return false;
        }
        
        String query = "SELECT id FROM voucher WHERE code=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, voucher);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            LOG.warn("Error checking for existence of voucher {}: {}", voucher, e.getMessage());
        }
        return false;
    }

    public boolean checkIfVoucherIsAssignedToUser(String voucher, int id) {
        if (voucher == null || voucher.trim().isEmpty()) {
            return false;
        }
        
        String query1 = "SELECT username FROM users WHERE id=?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement1 = connection.prepareStatement(query1)) {
            statement1.setInt(1, id);
            ResultSet rs = statement1.executeQuery();
            if (rs.next()) {
                String username = rs.getString(1);
                String query2 = "SELECT id FROM voucher WHERE code=? AND code LIKE ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
                    preparedStatement.setString(1, voucher);
                    preparedStatement.setString(2, "%" + username + "%");
                    ResultSet set = preparedStatement.executeQuery();
                    return set.next();
                }
            }
        } catch (SQLException e) {
            LOG.warn("Error verifying allocation of voucher {} to user with ID {}: {}", voucher, id, e.getMessage());
        }
        return false;
    }

    public void deleteVoucher(String voucher) {
        if (voucher == null || voucher.trim().isEmpty()) {
            return;
        }
        
        String query = "DELETE FROM voucher WHERE code=?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, voucher);
            statement.executeUpdate();
            
            AUDIT_LOGGER.audit("Successfully deleted voucher with code '" + voucher + "'");
        } catch (SQLException e) {
            LOG.warn("Failed to delete voucher with code {}", voucher, e);
        }
    }

    public List<Voucher> getAll() {
        List<Voucher> vouchers = new ArrayList<>();
        String query = "SELECT id, code, value FROM voucher";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                vouchers.add(new Voucher(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            LOG.warn("Error retrieving all vouchers: {}", e.getMessage());
        }
        return vouchers;
    }

}
