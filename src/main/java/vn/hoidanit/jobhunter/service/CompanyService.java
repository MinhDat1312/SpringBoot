package vn.hoidanit.jobhunter.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {
    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public ArrayList<Company> handleGetCompanies() {
        return (ArrayList<Company>) this.companyRepository.findAll();
    }

    public Company handleGetCompanyById(Long id) {
        Optional<Company> company = this.companyRepository.findById(id);

        if (company.isPresent()) {
            return company.get();
        }
        return null;
    }

    public Company handelUpdateCompany(Company company) {
        Company res = this.handleGetCompanyById(company.getId());

        if (res != null) {
            res.setName(company.getName());
            res.setDescription(company.getDescription());
            res.setAddress(company.getAddress());
            res.setLogo(company.getLogo());

            return this.companyRepository.save(res);
        }
        return null;
    }

    public void handleDeleteCompany(Long id) {
        this.companyRepository.deleteById(id);
    }
}
