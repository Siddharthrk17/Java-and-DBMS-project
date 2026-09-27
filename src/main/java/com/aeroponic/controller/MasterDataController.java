package com.aeroponic.controller;

import com.aeroponic.model.ChartOfAccount;
import com.aeroponic.model.Contact;
import com.aeroponic.model.Product;
import com.aeroponic.repository.ChartOfAccountRepository;
import com.aeroponic.repository.ContactRepository;
import com.aeroponic.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final ChartOfAccountRepository accountRepository;

    public MasterDataController(ContactRepository contactRepository,
                                ProductRepository productRepository,
                                ChartOfAccountRepository accountRepository) {
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
    }

    // --- CONTACTS (SUPERMARKETS & SUPPLIERS) ---
    @GetMapping("/contacts")
    public ResponseEntity<List<Contact>> getContacts(@RequestParam(required = false) Contact.ContactType type) {
        if (type != null) {
            return ResponseEntity.ok(contactRepository.findByType(type));
        }
        return ResponseEntity.ok(contactRepository.findAll());
    }

    @PostMapping("/contacts")
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.ok(contactRepository.save(contact));
    }

    // --- PRODUCTS ---
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }

    // --- CHART OF ACCOUNTS ---
    @GetMapping("/accounts")
    public ResponseEntity<List<ChartOfAccount>> getAccounts(@RequestParam(required = false) ChartOfAccount.AccountType type) {
        if (type != null) {
            return ResponseEntity.ok(accountRepository.findByAccountType(type));
        }
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @PostMapping("/accounts")
    public ResponseEntity<ChartOfAccount> createAccount(@RequestBody ChartOfAccount account) {
        return ResponseEntity.ok(accountRepository.save(account));
    }
}
