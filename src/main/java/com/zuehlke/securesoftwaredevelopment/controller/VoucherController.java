package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.repository.VoucherRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VoucherController {

    private final VoucherRepository voucherRepository;

    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/new-voucher")
    @PreAuthorize("hasAuthority('NEW_VOUCHER')")
    public String showAddVoucher(Model model) {
        model.addAttribute("vouchers", voucherRepository.getAll());
        return "new-voucher";
    }

    @PostMapping("/new-voucher")
    @PreAuthorize("hasAuthority('NEW_VOUCHER')")
    public String addVoucher(@RequestParam("value") int value, @RequestParam("code") String code) {
        if (code == null || code.trim().isEmpty() || value <= 0) {
            return "redirect:/new-voucher";
        }
        voucherRepository.create(code.trim(), value);
        return "redirect:/new-voucher";
    }
}
