package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.domain.Voucher;
import com.zuehlke.securesoftwaredevelopment.repository.PersonRepository;
import com.zuehlke.securesoftwaredevelopment.repository.RatingRepository;
import com.zuehlke.securesoftwaredevelopment.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class VoucherController {

    private static final Logger LOG = LoggerFactory.getLogger(VoucherController.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(VoucherController.class);

    private VoucherRepository voucherRepository;

    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/new-voucher")
    @PreAuthorize("hasAuthority('NEW_VOUCHER')")
    public String showAddVoucher(Model model) {
        List<Voucher> voucherList = voucherRepository.getAll();
        model.addAttribute("vouchers", voucherList);
        return "new-voucher";
    }

    @PostMapping("/new-voucher")
    @PreAuthorize("hasAuthority('NEW_VOUCHER')")
    public String addVoucher(@RequestParam("value") int value,
                             @RequestParam("code") String code) {
        // Validate input parameters
        if (code == null || code.trim().isEmpty() || value <= 0) {
            return "redirect:/new-voucher";
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            voucherRepository.create(user.getId(), code, value);
        }
        return "redirect:/new-voucher";
    }

}
