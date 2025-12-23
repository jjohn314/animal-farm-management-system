import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.image.BufferedImage;
import java.util.List;

// Animal class to store livestock details
class Animal {
    private String id;
    private String breed;
    private int age;
    private double weight;
    private String rfidTag;
    private String healthStatus;
    private String feedingData;
    private LocalDate lastUpdated;

    public Animal(String id, String breed, int age, double weight, String rfidTag) {
        this.id = id;
        this.breed = breed;
        this.age = age;
        this.weight = weight;
        this.rfidTag = rfidTag;
        this.healthStatus = "Healthy";
        this.feedingData = "No feeding data yet";
        this.lastUpdated = LocalDate.now();
    }

    public String getId() { return id; }
    public String getBreed() { return breed; }
    public int getAge() { return age; }
    public double getWeight() { return weight; }
    public String getRFIDTag() { return rfidTag; }
    public String getHealthStatus() { return healthStatus; }
    public String getFeedingData() { return feedingData; }
    public LocalDate getLastUpdated() { return lastUpdated; }

    public void updateHealthStatus(String status) {
        this.healthStatus = status;
        this.lastUpdated = LocalDate.now();
    }

    public void logFeedingData(String data) {
        this.feedingData = data;
        this.lastUpdated = LocalDate.now();
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Breed: " + breed + ", Age: " + age + ", Weight: " + weight + "kg, RFID: " + rfidTag +
                ", Health: " + healthStatus + ", Feeding: " + feedingData + ", Updated: " + lastUpdated;
    }

    public static Animal fromString(String line) {
        try {
            String[] parts = line.split(", ");
            if (parts.length < 7) return null; // Ensure data integrity

            String id = parts[0].split(": ")[1];
            String breed = parts[1].split(": ")[1];
            int age = Integer.parseInt(parts[2].split(": ")[1]);
            double weight = Double.parseDouble(parts[3].split(": ")[1].replace("kg", ""));
            String rfidTag = parts[4].split(": ")[1];
            String healthStatus = parts[5].split(": ")[1];
            String feedingData = parts[6].split(": ")[1];

            Animal animal = new Animal(id, breed, age, weight, rfidTag);
            animal.updateHealthStatus(healthStatus);
            animal.logFeedingData(feedingData);

            // Parse last updated date if available
            if (parts.length > 7) {
                try {
                    LocalDate date = LocalDate.parse(parts[7].split(": ")[1]);
                    animal.lastUpdated = date;
                } catch (Exception e) {
                    // Keep default date if parsing fails
                }
            }

            return animal;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error parsing animal data: " + e.getMessage());
            return null;
        }
    }
}

// Livestock Management System
class LivestockManagement {
    private Map<String, Animal> animals = new HashMap<>();
    private static final String DATA_FILE = "FarmRecords_v2.fdf";

    public LivestockManagement() {
        loadDataFromFile(); // Ensure data is loaded at startup
    }

    public void registerAnimal(String id, String breed, int age, double weight, String rfidTag) {
        if (animals.containsKey(id)) {
            JOptionPane.showMessageDialog(null, "Error: Animal with this ID already exists.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Animal animal = new Animal(id, breed, age, weight, rfidTag);
        animals.put(id, animal);
        saveDataToFile();
        JOptionPane.showMessageDialog(null, "Animal Registered: " + animal.getId(),
                "Registration Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public String scanRFIDTag(String rfidTag) {
        for (Animal animal : animals.values()) {
            if (animal.getRFIDTag().equals(rfidTag)) {
                return animal.toString();
            }
        }
        return "No animal found for RFID tag: " + rfidTag;
    }

    public java.util.List<Animal> getAllAnimals() {
        return new ArrayList<>(animals.values());
    }

    public void updateHealthStatus(String id, String status) {
        if (!animals.containsKey(id)) {
            JOptionPane.showMessageDialog(null, "Error: No animal found with this ID.",
                    "Update Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        animals.get(id).updateHealthStatus(status);
        saveDataToFile();
        JOptionPane.showMessageDialog(null, "Health status updated for: " + id,
                "Update Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void logFeedingData(String id, String data) {
        if (!animals.containsKey(id)) {
            JOptionPane.showMessageDialog(null, "Error: No animal found with this ID.",
                    "Update Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        animals.get(id).logFeedingData(data);
        saveDataToFile();
        JOptionPane.showMessageDialog(null, "Feeding data logged for: " + id,
                "Update Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public Animal getAnimalById(String id) {
        return animals.get(id);
    }

    public String generateFarmActivitySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Farm Activity Summary - ").append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n\n");
        summary.append("Total animals: ").append(animals.size()).append("\n\n");

        // Count animals by health status
        Map<String, Integer> healthStats = new HashMap<>();
        for (Animal animal : animals.values()) {
            String status = animal.getHealthStatus();
            healthStats.put(status, healthStats.getOrDefault(status, 0) + 1);
        }

        summary.append("Health Status Summary:\n");
        for (Map.Entry<String, Integer> entry : healthStats.entrySet()) {
            summary.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        summary.append("\nAnimal Details:\n");
        for (Animal animal : animals.values()) {
            summary.append("- ").append(animal).append("\n");
        }

        return summary.toString();
    }

    private void saveDataToFile() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            for (Animal animal : animals.values()) {
                writer.write(animal.toString() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving data to file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDataFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return; // No file to load, start with empty map
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Animal animal = Animal.fromString(line);
                if (animal != null) {
                    animals.put(animal.getId(), animal);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading data from file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// User class to store user credentials and permissions
class User {
    private String username;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private LocalDate lastLogin;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.passwordHash = hashPassword(password);
        this.role = role;
        this.active = true;
        this.lastLogin = LocalDate.now();
    }

    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDate getLastLogin() { return lastLogin; }

    public void setRole(UserRole role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void updateLastLogin() { this.lastLogin = LocalDate.now(); }

    public boolean checkPassword(String password) {
        return hashPassword(password).equals(passwordHash);
    }

    public void changePassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }

    private String hashPassword(String password) {
        // Simple hash for demonstration - in a real app use a proper hashing algorithm!
        return String.valueOf(password.hashCode());
    }

    @Override
    public String toString() {
        return username + "," + passwordHash + "," + role + "," + active + "," + lastLogin;
    }

    public static User fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) return null;

        User user = new User(parts[0], "", UserRole.valueOf(parts[2]));
        user.passwordHash = parts[1];
        user.active = Boolean.parseBoolean(parts[3]);
        user.lastLogin = LocalDate.parse(parts[4]);
        return user;
    }
}

// User roles enum
enum UserRole {
    ADMIN("Administrator", true, true, true, true),
    MANAGER("Manager", true, true, true, false),
    STAFF("Staff", true, true, false, false),
    VETERINARIAN("Veterinarian", true, false, true, false),
    VIEWER("Viewer", false, false, false, false);

    private String title;
    private boolean canEdit;
    private boolean canRegister;
    private boolean canUpdateHealth;
    private boolean canManageUsers;

    UserRole(String title, boolean canEdit, boolean canRegister, boolean canUpdateHealth, boolean canManageUsers) {
        this.title = title;
        this.canEdit = canEdit;
        this.canRegister = canRegister;
        this.canUpdateHealth = canUpdateHealth;
        this.canManageUsers = canManageUsers;
    }

    public String getTitle() { return title; }
    public boolean canEdit() { return canEdit; }
    public boolean canRegister() { return canRegister; }
    public boolean canUpdateHealth() { return canUpdateHealth; }
    public boolean canManageUsers() { return canManageUsers; }
}

// User Management System
class UserManagement {
    private Map<String, User> users = new HashMap<>();
    private User currentUser = null;
    private static final String USER_FILE = "users.txt";

    public UserManagement() {
        // Create default admin if no users exist
        loadUsers();
        if (users.isEmpty()) {
            User admin = new User("admin", "admin", UserRole.ADMIN);
            users.put(admin.getUsername(), admin);
            saveUsers();
        }
    }

    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.isActive() && user.checkPassword(password)) {
            currentUser = user;
            user.updateLastLogin();
            saveUsers();
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasPermission(String permission) {
        if (currentUser == null) return false;

        switch (permission) {
            case "edit": return currentUser.getRole().canEdit();
            case "register": return currentUser.getRole().canRegister();
            case "updateHealth": return currentUser.getRole().canUpdateHealth();
            case "manageUsers": return currentUser.getRole().canManageUsers();
            default: return false;
        }
    }

    public boolean addUser(String username, String password, UserRole role) {
        if (!hasPermission("manageUsers")) return false;
        if (users.containsKey(username)) return false;

        User newUser = new User(username, password, role);
        users.put(username, newUser);
        saveUsers();
        return true;
    }

    public boolean updateUserRole(String username, UserRole newRole) {
        if (!hasPermission("manageUsers")) return false;
        User user = users.get(username);
        if (user == null) return false;

        user.setRole(newRole);
        saveUsers();
        return true;
    }

    public boolean disableUser(String username) {
        if (!hasPermission("manageUsers")) return false;
        User user = users.get(username);
        if (user == null) return false;

        user.setActive(false);
        saveUsers();
        return true;
    }

    public boolean enableUser(String username) {
        if (!hasPermission("manageUsers")) return false;
        User user = users.get(username);
        if (user == null) return false;

        user.setActive(true);
        saveUsers();
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = users.get(username);
        if (user == null) return false;

        // If current user is admin, allow password change without old password
        // Otherwise, require old password to match
        if ((currentUser.getRole() == UserRole.ADMIN && hasPermission("manageUsers")) ||
                user.checkPassword(oldPassword)) {
            user.changePassword(newPassword);
            saveUsers();
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() {
        if (!hasPermission("manageUsers")) return new ArrayList<>();
        return new ArrayList<>(users.values());
    }

    private void saveUsers() {
        try (FileWriter writer = new FileWriter(USER_FILE)) {
            for (User user : users.values()) {
                writer.write(user.toString() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromString(line);
                if (user != null) {
                    users.put(user.getUsername(), user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
}

// GUI Application
public class LivestockGUI {
    private LivestockManagement system = new LivestockManagement();
    private JFrame frame;
    private JPanel mainPanel;
    private JTable animalTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // GUI theme colors
    private static final Color PRIMARY_COLOR = new Color(46, 134, 193);
    private static final Color SECONDARY_COLOR = new Color(214, 234, 248);
    private static final Color HIGHLIGHT_COLOR = new Color(88, 214, 141);

    public LivestockGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Create main frame
        frame = new JFrame("Livestock Management System");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set custom icon
        ImageIcon icon = createIcon(16, 16, PRIMARY_COLOR);
        frame.setIconImage(icon.getImage());

        // Create main panel with border layout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Create and add header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create and add sidebar panel
        JPanel sidebarPanel = createSidebarPanel();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Create and add content panel
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Create and add status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        // Add main panel to frame
        frame.add(mainPanel);

        // Center frame on screen
        frame.setLocationRelativeTo(null);

        // Make frame visible
        frame.setVisible(false);

        // Update the table with current data
        refreshAnimalTable();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel titleLabel = new JLabel("Livestock Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.WEST);

        // Add search box
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);

        searchField = new JTextField(15);
        JButton searchButton = createStyledButton("Search", null);
        searchButton.addActionListener(e -> searchAnimals());

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 0, 10));
        panel.setBackground(SECONDARY_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setPreferredSize(new Dimension(200, 0));

        JButton registerButton = createMenuButton("Register Animal", "add");
        JButton scanButton = createMenuButton("Scan RFID", "search");
        JButton displayButton = createMenuButton("Display All", "list");
        JButton updateHealthButton = createMenuButton("Update Health", "health");
        JButton logFeedingButton = createMenuButton("Log Feeding", "food");
        JButton generateSummaryButton = createMenuButton("Farm Summary", "report");
        JButton exitButton = createMenuButton("Exit", "exit");

        registerButton.addActionListener(e -> showRegisterDialog());
        scanButton.addActionListener(e -> showScanDialog());
        displayButton.addActionListener(e -> refreshAnimalTable());
        updateHealthButton.addActionListener(e -> showUpdateHealthDialog());
        logFeedingButton.addActionListener(e -> showLogFeedingDialog());
        generateSummaryButton.addActionListener(e -> showFarmSummary());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(registerButton);
        panel.add(scanButton);
        panel.add(displayButton);
        panel.add(updateHealthButton);
        panel.add(logFeedingButton);
        panel.add(generateSummaryButton);
        panel.add(exitButton);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Create table model with columns
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        tableModel.addColumn("ID");
        tableModel.addColumn("Breed");
        tableModel.addColumn("Age");
        tableModel.addColumn("Weight (kg)");
        tableModel.addColumn("RFID Tag");
        tableModel.addColumn("Health Status");
        tableModel.addColumn("Last Updated");

        // Create table with the model
        animalTable = new JTable(tableModel);
        animalTable.setRowHeight(25);
        animalTable.setGridColor(Color.LIGHT_GRAY);
        animalTable.getTableHeader().setBackground(PRIMARY_COLOR);
        animalTable.getTableHeader().setForeground(Color.BLACK);
        animalTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        animalTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        // Add selection listener
        animalTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && animalTable.getSelectedRow() != -1) {
                showAnimalDetails(animalTable.getValueAt(animalTable.getSelectedRow(), 0).toString());
            }
        });

        // Add scroll pane for table
        JScrollPane scrollPane = new JScrollPane(animalTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add title above the table
        JLabel tableTitle = new JLabel("Registered Animals");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(tableTitle, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setBackground(SECONDARY_COLOR);

        JLabel dateLabel = new JLabel("Date: " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        panel.add(dateLabel, BorderLayout.EAST);

        return panel;
    }

    private JButton createMenuButton(String text, String iconType) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HIGHLIGHT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
        });

        return button;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);

        if (backgroundColor != null) {
            button.setBackground(backgroundColor);
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setBorderPainted(false);
        }

        return button;
    }

    private ImageIcon createIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(color);
        g2d.fillOval(0, 0, width, height);
        g2d.dispose();

        return new ImageIcon(image);
    }

    private void refreshAnimalTable() {
        // Clear the table
        tableModel.setRowCount(0);

        // Get all animals and sort them by ID
        ArrayList<Animal> sortedAnimals = new ArrayList<>(system.getAllAnimals());
        Collections.sort(sortedAnimals, Comparator.comparing(Animal::getId));

        // Populate table with animal data
        for (Animal animal : sortedAnimals) {
            Object[] row = new Object[7];
            row[0] = animal.getId();
            row[1] = animal.getBreed();
            row[2] = animal.getAge();
            row[3] = animal.getWeight();
            row[4] = animal.getRFIDTag();
            row[5] = animal.getHealthStatus();
            row[6] = animal.getLastUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE);

            tableModel.addRow(row);
        }

        // Apply alternating row colors
        animalTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(255, 255, 255, 255));
                }

                // Highlight health status based on value
                if (column == 5 && value != null) {
                    String status = value.toString();
                    if (status.equalsIgnoreCase("Healthy")) {
                        c.setForeground(new Color(0, 128, 0)); // Dark green
                    } else if (status.toLowerCase().contains("sick") || status.toLowerCase().contains("ill")) {
                        c.setForeground(new Color(192, 0, 0)); // Dark red
                    } else {
                        c.setForeground(new Color(128, 128, 0)); // Olive for other states
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });
    }

    private void searchAnimals() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshAnimalTable();
            return;
        }

        // Clear the table
        tableModel.setRowCount(0);

        // Filter and populate table with matching animal data
        for (Animal animal : system.getAllAnimals()) {
            if (animal.getId().toLowerCase().contains(query) ||
                    animal.getRFIDTag().toLowerCase().contains(query) ||
                    animal.getBreed().toLowerCase().contains(query) ||
                    animal.getHealthStatus().toLowerCase().contains(query)) {

                Object[] row = new Object[7];
                row[0] = animal.getId();
                row[1] = animal.getBreed();
                row[2] = animal.getAge();
                row[3] = animal.getWeight();
                row[4] = animal.getRFIDTag();
                row[5] = animal.getHealthStatus();
                row[6] = animal.getLastUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE);

                tableModel.addRow(row);
            }
        }
    }

    private void showAnimalDetails(String id) {
        Animal animal = system.getAnimalById(id);
        if (animal == null) return;

        JDialog dialog = new JDialog(frame, "Animal Details", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(frame);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        addDetailRow(detailsPanel, "ID:", animal.getId());
        addDetailRow(detailsPanel, "Breed:", animal.getBreed());
        addDetailRow(detailsPanel, "Age:", String.valueOf(animal.getAge()));
        addDetailRow(detailsPanel, "Weight:", animal.getWeight() + " kg");
        addDetailRow(detailsPanel, "RFID Tag:", animal.getRFIDTag());
        addDetailRow(detailsPanel, "Health Status:", animal.getHealthStatus());
        addDetailRow(detailsPanel, "Feeding Data:", animal.getFeedingData());
        addDetailRow(detailsPanel, "Last Updated:", animal.getLastUpdated().format(DateTimeFormatter.ISO_LOCAL_DATE));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JButton updateHealthButton = createStyledButton("Update Health", PRIMARY_COLOR);
        JButton logFeedingButton = createStyledButton("Log Feeding", PRIMARY_COLOR);
        JButton closeButton = createStyledButton("Close", null);

        updateHealthButton.addActionListener(e -> {
            String status = JOptionPane.showInputDialog(dialog, "Enter New Health Status:", animal.getHealthStatus());
            if (status != null && !status.trim().isEmpty()) {
                system.updateHealthStatus(animal.getId(), status);
                dialog.dispose();
                refreshAnimalTable();
            }
        });

        logFeedingButton.addActionListener(e -> {
            String data = JOptionPane.showInputDialog(dialog, "Enter Feeding Data:", animal.getFeedingData());
            if (data != null && !data.trim().isEmpty()) {
                system.logFeedingData(animal.getId(), data);
                dialog.dispose();
                refreshAnimalTable();
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(updateHealthButton);
        buttonPanel.add(logFeedingButton);
        buttonPanel.add(closeButton);

        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 14));

        panel.add(lblName);
        panel.add(lblValue);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(frame, "Register New Animal", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(frame);

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField idField = new JTextField(15);
        JTextField breedField = new JTextField(15);
        JTextField ageField = new JTextField(15);
        JTextField weightField = new JTextField(15);
        JTextField rfidField = new JTextField(15);

        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Breed:"));
        formPanel.add(breedField);
        formPanel.add(new JLabel("Age:"));
        formPanel.add(ageField);
        formPanel.add(new JLabel("Weight (kg):"));
        formPanel.add(weightField);
        formPanel.add(new JLabel("RFID Tag:"));
        formPanel.add(rfidField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JButton registerButton = createStyledButton("Register", PRIMARY_COLOR);
        JButton cancelButton = createStyledButton("Cancel", null);

        registerButton.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String breed = breedField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                double weight = Double.parseDouble(weightField.getText().trim());
                String rfid = rfidField.getText().trim();

                if (id.isEmpty() || breed.isEmpty() || rfid.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                system.registerAnimal(id, breed, age, weight, rfid);
                dialog.dispose();
                refreshAnimalTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for age and weight.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showScanDialog() {
        String rfidTag = JOptionPane.showInputDialog(frame, "Enter RFID Tag to Scan:", "Scan RFID", JOptionPane.QUESTION_MESSAGE);
        if (rfidTag != null && !rfidTag.trim().isEmpty()) {
            String result = system.scanRFIDTag(rfidTag);
            JOptionPane.showMessageDialog(frame, result, "Scan Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showUpdateHealthDialog() {
        if (animalTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an animal from the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = animalTable.getValueAt(animalTable.getSelectedRow(), 0).toString();
        String status = JOptionPane.showInputDialog(frame, "Enter New Health Status for Animal " + id + ":",
                "Update Health Status", JOptionPane.QUESTION_MESSAGE);

        if (status != null && !status.trim().isEmpty()) {
            system.updateHealthStatus(id, status);
            refreshAnimalTable();
        }
    }

    private void showLogFeedingDialog() {
        if (animalTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an animal from the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = animalTable.getValueAt(animalTable.getSelectedRow(), 0).toString();
        String data = JOptionPane.showInputDialog(frame, "Enter Feeding Data for Animal " + id + ":",
                "Log Feeding Data", JOptionPane.QUESTION_MESSAGE);

        if (data != null && !data.trim().isEmpty()) {
            system.logFeedingData(id, data);
            refreshAnimalTable();
        }
    }

    private void showFarmSummary() {
        String summary = system.generateFarmActivitySummary();

        JDialog dialog = new JDialog(frame, "Farm Activity Summary", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(frame);

        JTextArea textArea = new JTextArea(summary);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton printButton = createStyledButton("Print", PRIMARY_COLOR);
        JButton closeButton = createStyledButton("Close", null);

        printButton.addActionListener(e -> {
            try {
                textArea.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error printing: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private UserManagement userManager = new UserManagement();

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(frame, "Login", true);
        loginDialog.setLayout(new BorderLayout());
        loginDialog.setSize(300, 150);
        loginDialog.setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Exit");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (userManager.authenticate(username, password)) {
                loginDialog.dispose();
                updateUIBasedOnPermissions();
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Invalid username or password",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        loginDialog.add(loginPanel, BorderLayout.CENTER);
        loginDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Make Enter key submit the login form
        loginDialog.getRootPane().setDefaultButton(loginButton);

        loginDialog.setVisible(true);
        frame.setVisible(true);
    }

    private void updateUIBasedOnPermissions() {
        // Update the title to show current user
        frame.setTitle("Livestock Management System - Logged in as: " +
                userManager.getCurrentUser().getUsername() + " (" +
                userManager.getCurrentUser().getRole().getTitle() + ")");

        // Update buttons based on permissions
        boolean canEdit = userManager.hasPermission("edit");
        boolean canRegister = userManager.hasPermission("register");
        boolean canUpdateHealth = userManager.hasPermission("updateHealth");

        // Find buttons and enable/disable based on permissions
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component button : ((JPanel) comp).getComponents()) {
                    if (button instanceof JButton) {
                        JButton btn = (JButton) button;
                        String text = btn.getText();

                        if (text.contains("Register")) {
                            btn.setEnabled(canRegister);
                        } else if (text.contains("Update Health") || text.contains("Log Feeding")) {
                            btn.setEnabled(canUpdateHealth);
                        }
                    }
                }
            }
        }

        // Add logout and user management buttons if not already present
        JPanel sidebar = null;
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel && ((JPanel) comp).getLayout() instanceof GridLayout) {
                sidebar = (JPanel) comp;
                break;
            }
        }

        if (sidebar != null) {
            // Add user management button if admin
            if (userManager.hasPermission("manageUsers")) {
                JButton userManagementButton = createMenuButton("User Management", "users");
                userManagementButton.addActionListener(e -> showUserManagementDialog());
                sidebar.add(userManagementButton);
            }

            // Add logout button
            JButton logoutButton = createMenuButton("Logout", "logout");
            logoutButton.addActionListener(e -> {
                userManager.logout();
                frame.dispose();
                createAndShowGUI();
                showLoginDialog();
            });
            sidebar.add(logoutButton);

            sidebar.revalidate();
            sidebar.repaint();
        }
    }

    private void showUserManagementDialog() {
        if (!userManager.hasPermission("manageUsers")) {
            JOptionPane.showMessageDialog(frame, "You don't have permission to manage users.",
                    "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(frame, "User Management", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        // Create table model
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Username");
        model.addColumn("Role");
        model.addColumn("Status");
        model.addColumn("Last Login");

        // Fill table with users
        List<User> users = userManager.getAllUsers();
        for (User user : users) {
            model.addRow(new Object[]{
                    user.getUsername(),
                    user.getRole().getTitle(),
                    user.isActive() ? "Active" : "Disabled",
                    user.getLastLogin().format(DateTimeFormatter.ISO_LOCAL_DATE)
            });
        }

        // Create table
        JTable userTable = new JTable(model);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Create buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit Role");
        JButton enableDisableButton = new JButton("Enable/Disable");
        JButton resetPasswordButton = new JButton("Reset Password");
        JButton closeButton = new JButton("Close");

        // Add action listeners
        addButton.addActionListener(e -> showAddUserDialog(dialog));

        editButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) model.getValueAt(selectedRow, 0);
                showEditUserRoleDialog(dialog, username);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a user first.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        enableDisableButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) model.getValueAt(selectedRow, 0);
                String status = (String) model.getValueAt(selectedRow, 2);
                boolean currentlyActive = status.equals("Active");

                if (userManager.getCurrentUser().getUsername().equals(username)) {
                    JOptionPane.showMessageDialog(dialog, "You cannot disable your own account.",
                            "Operation Not Allowed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (currentlyActive) {
                    userManager.disableUser(username);
                } else {
                    userManager.enableUser(username);
                }

                // Refresh table
                model.setValueAt(currentlyActive ? "Disabled" : "Active", selectedRow, 2);
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a user first.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        resetPasswordButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) model.getValueAt(selectedRow, 0);
                String newPassword = JOptionPane.showInputDialog(dialog,
                        "Enter new password for " + username + ":", "Reset Password",
                        JOptionPane.QUESTION_MESSAGE);

                if (newPassword != null && !newPassword.isEmpty()) {
                    userManager.changePassword(username, "", newPassword);
                    JOptionPane.showMessageDialog(dialog, "Password reset successfully.",
                            "Password Reset", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a user first.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        // Add buttons to panel
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(enableDisableButton);
        buttonPanel.add(resetPasswordButton);
        buttonPanel.add(closeButton);

        // Add components to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showAddUserDialog(JDialog parent) {
        JDialog dialog = new JDialog(parent, "Add New User", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>();

        for (UserRole role : UserRole.values()) {
            roleCombo.addItem(role.getTitle());
        }

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String roleTitle = (String) roleCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password are required.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            UserRole role = null;
            for (UserRole r : UserRole.values()) {
                if (r.getTitle().equals(roleTitle)) {
                    role = r;
                    break;
                }
            }

            if (role != null && userManager.addUser(username, password, role)) {
                JOptionPane.showMessageDialog(dialog, "User added successfully.",
                        "User Added", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                parent.dispose();
                showUserManagementDialog();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add user. Username may already exist.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showEditUserRoleDialog(JDialog parent, String username) {
        JDialog dialog = new JDialog(parent, "Edit User Role", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> roleCombo = new JComboBox<>();
        for (UserRole role : UserRole.values()) {
            roleCombo.addItem(role.getTitle());
        }

        formPanel.add(new JLabel("Username:"));
        formPanel.add(new JLabel(username));
        formPanel.add(new JLabel("New Role:"));
        formPanel.add(roleCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");

        updateButton.addActionListener(e -> {
            String roleTitle = (String) roleCombo.getSelectedItem();

            UserRole role = null;
            for (UserRole r : UserRole.values()) {
                if (r.getTitle().equals(roleTitle)) {
                    role = r;
                    break;
                }
            }

            if (role != null && userManager.updateUserRole(username, role)) {
                JOptionPane.showMessageDialog(dialog, "User role updated successfully.",
                        "Role Updated", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                parent.dispose();
                showUserManagementDialog();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update user role.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LivestockGUI gui = new LivestockGUI();
            gui.showLoginDialog();
        });
    }
}
