package vn.hoidanit.jobhunter.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.CompanyService;

@RestController
public class CompanyController {
    private CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    public ResponseEntity<Object> createCompany(@Valid @RequestBody Company company) {
        Company res = this.companyService.handleCreateCompany(company);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/companies")
    public ResponseEntity<ResultPaginationDTO> getCompanies(
            @RequestParam("currentPage") Optional<String> currentPage,
            @RequestParam("pageSize") Optional<String> pageSize) {
        String sCurrentPage = currentPage.isPresent() ? currentPage.get() : "";
        String sPageSize = pageSize.isPresent() ? pageSize.get() : "";
        Pageable pageable = PageRequest.of(Integer.parseInt(sCurrentPage) - 1, Integer.parseInt(sPageSize));

        return ResponseEntity.status(HttpStatus.OK).body(this.companyService.handleGetCompanies(pageable));
    }

    @PutMapping("/companies")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company company) {
        Company c = this.companyService.handelUpdateCompany(company);

        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable("id") long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.ok(null);
    }
}
