package com.campusfind.lostfound.service;

import com.campusfind.lostfound.model.*;
import com.campusfind.lostfound.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DataInitializerService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final ItemReturnRepository itemReturnRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializerService(UserRepository userRepository,
                                  LostItemRepository lostItemRepository,
                                  FoundItemRepository foundItemRepository,
                                  ClaimRepository claimRepository,
                                  ItemReturnRepository itemReturnRepository,
                                  NotificationRepository notificationRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.claimRepository = claimRepository;
        this.itemReturnRepository = itemReturnRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println(">>> Initializing Smart Lost and Found database with campus seed data...");

        // 1. Create Users
        User admin = new User(
                "Prof. Arvind Swaminathan",
                "admin@college.edu",
                "ADM-001",
                "Campus Security & Student Affairs",
                "+91 98765 00001",
                passwordEncoder.encode("Admin@123"),
                Role.ADMIN
        );
        userRepository.save(admin);

        User student1 = new User(
                "Rahul Sharma",
                "rahul.sharma@college.edu",
                "CS-2024-042",
                "Computer Science & Engineering",
                "+91 98765 11101",
                passwordEncoder.encode("Student@123"),
                Role.STUDENT
        );
        userRepository.save(student1);

        User student2 = new User(
                "Priya Patel",
                "priya.patel@college.edu",
                "EC-2024-118",
                "Electronics & Communication",
                "+91 98765 22202",
                passwordEncoder.encode("Student@123"),
                Role.STUDENT
        );
        userRepository.save(student2);

        User staff = new User(
                "Dr. Anil Verma",
                "anil.verma@college.edu",
                "STF-ME-102",
                "Mechanical Engineering",
                "+91 98765 33303",
                passwordEncoder.encode("Staff@123"),
                Role.STAFF
        );
        userRepository.save(staff);

        User student3 = new User(
                "Sneha Reddy",
                "sneha.reddy@college.edu",
                "IT-2024-089",
                "Information Technology",
                "+91 98765 44404",
                passwordEncoder.encode("Student@123"),
                Role.STUDENT
        );
        userRepository.save(student3);

        LocalDate today = LocalDate.now();

        // 2. Lost Items
        LostItem lost1 = new LostItem();
        lost1.setUser(student1);
        lost1.setItemName("Black Leather Wallet");
        lost1.setCategory(Category.WALLETS);
        lost1.setDescription("Men's bifold WildHorn black leather wallet. Contains college ID card and bank card.");
        lost1.setDateLost(today.minusDays(2));
        lost1.setLocationLost("Central Campus Library, 2nd Floor");
        lost1.setColorFeatures("Black leather with white stitching along edges");
        lost1.setImageUrl("/images/items/wallet.svg");
        lost1.setContactPhone("+91 98765 11101");
        lost1.setStatus(LostStatus.REPORTED);
        lostItemRepository.save(lost1);

        LostItem lost2 = new LostItem();
        lost2.setUser(student2);
        lost2.setItemName("College Student Identity Card");
        lost2.setCategory(Category.ID_CARD);
        lost2.setDescription("Official college smart ID card in transparent acrylic card holder with blue campus lanyard.");
        lost2.setDateLost(today.minusDays(3));
        lost2.setLocationLost("Science Block Lecture Hall 3");
        lost2.setColorFeatures("Blue university lanyard with silver clip");
        lost2.setImageUrl("/images/items/idcard.svg");
        lost2.setContactPhone("+91 98765 22202");
        lost2.setStatus(LostStatus.REPORTED);
        lostItemRepository.save(lost2);

        LostItem lost3 = new LostItem();
        lost3.setUser(student3);
        lost3.setItemName("HP Wireless Bluetooth Mouse");
        lost3.setCategory(Category.ELECTRONICS);
        lost3.setDescription("HP 240 silent Bluetooth mouse. Small scratch near the scroll wheel.");
        lost3.setDateLost(today.minusDays(4));
        lost3.setLocationLost("Computer Center Lab 2");
        lost3.setColorFeatures("Matte dark grey finish");
        lost3.setImageUrl("/images/items/mouse.svg");
        lost3.setContactPhone("+91 98765 44404");
        lost3.setStatus(LostStatus.REPORTED);
        lostItemRepository.save(lost3);

        LostItem lost4 = new LostItem();
        lost4.setUser(staff);
        lost4.setItemName("Casio fx-991EX Scientific Calculator");
        lost4.setCategory(Category.ELECTRONICS);
        lost4.setDescription("ClassWiz scientific calculator with sliding hard cover. Has a sticker 'ME-Lab' on the back.");
        lost4.setDateLost(today.minusDays(1));
        lost4.setLocationLost("Mechanical Workshop Seminar Room");
        lost4.setColorFeatures("Black body with silver patterned front face");
        lost4.setImageUrl("/images/items/calculator.svg");
        lost4.setContactPhone("+91 98765 33303");
        lost4.setStatus(LostStatus.REPORTED);
        lostItemRepository.save(lost4);

        LostItem lost5 = new LostItem();
        lost5.setUser(student1);
        lost5.setItemName("Dell Urban Backpack");
        lost5.setCategory(Category.BAGS);
        lost5.setDescription("Water-resistant navy blue Dell laptop backpack with 2 side mesh bottle pockets.");
        lost5.setDateLost(today.minusDays(5));
        lost5.setLocationLost("Campus Cafeteria Food Court");
        lost5.setColorFeatures("Navy blue with reflective Dell logo strip");
        lost5.setImageUrl("/images/items/backpack.svg");
        lost5.setContactPhone("+91 98765 11101");
        lost5.setStatus(LostStatus.REPORTED);
        lostItemRepository.save(lost5);

        // 3. Found Items
        FoundItem found1 = new FoundItem();
        found1.setUser(student2);
        found1.setItemName("Black Leather Wallet");
        found1.setCategory(Category.WALLETS);
        found1.setDescription("Black bifold wallet picked up from study table row 4. Handed over directly to reception.");
        found1.setDateFound(today.minusDays(2));
        found1.setLocationFound("Central Campus Library, 2nd Floor");
        found1.setColorFeatures("Black leather with perimeter white stitch pattern");
        found1.setImageUrl("/images/items/wallet.svg");
        found1.setStorageLocation("Campus Security Desk - Admin Block Room 102");
        found1.setStatus(FoundStatus.CLAIM_PENDING);
        foundItemRepository.save(found1);

        FoundItem found2 = new FoundItem();
        found2.setUser(staff);
        found2.setItemName("Student Identity Card with Blue Lanyard");
        found2.setCategory(Category.ID_CARD);
        found2.setDescription("Student smart RFID card found dropped between seating rows in science lecture hall.");
        found2.setDateFound(today.minusDays(3));
        found2.setLocationFound("Science Block Lecture Hall 3");
        found2.setColorFeatures("Blue lanyard, EC Department card inside");
        found2.setImageUrl("/images/items/idcard.svg");
        found2.setStorageLocation("Student Affairs Information Helpdesk");
        found2.setStatus(FoundStatus.AVAILABLE);
        foundItemRepository.save(found2);

        FoundItem found3 = new FoundItem();
        found3.setUser(student3);
        found3.setItemName("Apple AirPods Pro 2nd Gen");
        found3.setCategory(Category.ELECTRONICS);
        found3.setDescription("AirPods in white wireless charging case with a small red silicone ring strap attached.");
        found3.setDateFound(today.minusDays(1));
        found3.setLocationFound("Campus Sports Complex Badminton Court");
        found3.setColorFeatures("White case with red protective silicone case");
        found3.setImageUrl("/images/items/airpods.svg");
        found3.setStorageLocation("Campus Security Desk - Admin Block Room 102");
        found3.setStatus(FoundStatus.AVAILABLE);
        foundItemRepository.save(found3);

        FoundItem found4 = new FoundItem();
        found4.setUser(student1);
        found4.setItemName("Stainless Steel Insulated Water Bottle");
        found4.setCategory(Category.OTHER);
        found4.setDescription("750ml Milton Thermosteel insulated bottle with carry handle cap.");
        found4.setDateFound(today.minusDays(2));
        found4.setLocationFound("Library Main Reading Hall");
        found4.setColorFeatures("Silver metallic stainless steel");
        found4.setImageUrl("/images/items/bottle.svg");
        found4.setStorageLocation("Library Reception Cloakroom Counter");
        found4.setStatus(FoundStatus.AVAILABLE);
        foundItemRepository.save(found4);

        FoundItem found5 = new FoundItem();
        found5.setUser(student2);
        found5.setItemName("Set of 3 Keys with Batman Keychain");
        found5.setCategory(Category.KEYS);
        found5.setDescription("Two brass Godrej door keys and one bike key attached to a metal Batman keychain.");
        found5.setDateFound(today.minusDays(4));
        found5.setLocationFound("Main Parking Lot B Near Gate 2");
        found5.setColorFeatures("Silver & black Batman metal keychain");
        found5.setImageUrl("/images/items/keys.svg");
        found5.setStorageLocation("Main Gate Security Post 1");
        found5.setStatus(FoundStatus.RETURNED);
        foundItemRepository.save(found5);

        // 4. Sample Claim
        Claim claim1 = new Claim();
        claim1.setFoundItem(found1);
        claim1.setClaimant(student1);
        claim1.setItemDescriptionProof("WildHorn brand men's wallet with coin pocket and driving license pocket.");
        claim1.setUniqueIdentifyingFeatures("Contains student ID card CS-2024-042 (Rahul Sharma) and an SBI Global debit card ending in digits 4102.");
        claim1.setApproxDateLost(today.minusDays(2));
        claim1.setApproxLocationLost("Central Library 2nd Floor study desk");
        claim1.setSupportingProofDetails("I can show matching DigiLocker student ID and debit card SMS alerts on my phone.");
        claim1.setStatus(ClaimStatus.UNDER_VERIFICATION);
        claim1.setAdminRemarks("Claimant details match internal cards reported by security officer. Scheduled for verification at security desk.");
        claimRepository.save(claim1);

        // 5. Sample Returned Item & Completed Claim
        Claim claimReturned = new Claim();
        claimReturned.setFoundItem(found5);
        claimReturned.setClaimant(student3);
        claimReturned.setItemDescriptionProof("Set of room keys with black Batman keychain.");
        claimReturned.setUniqueIdentifyingFeatures("Bike ignition key is for Honda Activa with number plate ending in 8841.");
        claimReturned.setApproxDateLost(today.minusDays(4));
        claimReturned.setApproxLocationLost("Parking Lot B");
        claimReturned.setSupportingProofDetails("Brought bike RC smartcard matching the ignition key.");
        claimReturned.setStatus(ClaimStatus.RETURNED);
        claimReturned.setAdminRemarks("Identity verified via RC card. Handover completed safely.");
        claimRepository.save(claimReturned);

        ItemReturn returnRecord = new ItemReturn(
                claimReturned,
                found5,
                student3,
                admin,
                "Keys handed over to Sneha Reddy after verifying Honda Activa registration plate 8841."
        );
        itemReturnRepository.save(returnRecord);

        // 6. Seed Notifications
        Notification notif1 = new Notification(
                student1,
                "Potential Match: Black Leather Wallet",
                "A found item 'Black Leather Wallet' at 'Central Campus Library, 2nd Floor' matches your lost item with 92% confidence score. Review your dashboard.",
                NotificationType.POTENTIAL_MATCH,
                found1.getFoundId()
        );
        notificationRepository.save(notif1);

        Notification notif2 = new Notification(
                student2,
                "Potential Match: College Student Identity Card",
                "A found item 'Student Identity Card with Blue Lanyard' matches your report with 88% confidence score.",
                NotificationType.POTENTIAL_MATCH,
                found2.getFoundId()
        );
        notificationRepository.save(notif2);

        Notification notif3 = new Notification(
                student1,
                "Claim Under Verification",
                "Your claim for 'Black Leather Wallet' is under verification with Campus Security. Please monitor for approval.",
                NotificationType.CLAIM_UPDATE,
                claim1.getClaimId()
        );
        notificationRepository.save(notif3);

        System.out.println(">>> Seed data initialized successfully!");
    }
}
