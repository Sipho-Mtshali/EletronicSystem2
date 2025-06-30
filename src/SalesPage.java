import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SalesPage extends javax.swing.JFrame {
    // Database connection variables
    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;
    
    public SalesPage() {
        initComponents();
        connectDatabase();
        loadProducts();
        loadCustomers();
        loadSalesData();
        setupCalculations();
        setupTableClickListener();
        
        // Initially disable the total amount field
        txtTA.setEditable(false);
    }
private void connectDatabase() {
        try {
            // Update these credentials according to your database setup
            String url = "jdbc:mysql://localhost:3306/ElectronicSystemDb";
            String username = "root";
            String password = "12345678";
            
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, username, password);
            
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Loads all products from database into the product combo box
     */
    private void loadProducts() {
        try {
            if (con != null && !con.isClosed()) {
                String sql = "SELECT product_name FROM productstable ORDER BY product_name";
                pst = con.prepareStatement(sql);
                rs = pst.executeQuery();
                
                jComboBox2.removeAllItems();
                jComboBox2.addItem("Select Product");
                
                while (rs.next()) {
                    jComboBox2.addItem(rs.getString("product_name"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    /**
     * Loads all customers from database into the customer combo box
     */
    private void loadCustomers() {
        try {
            if (con != null && !con.isClosed()) {
                String sql = "SELECT fullname FROM customerstable ORDER BY fullname";
                pst = con.prepareStatement(sql);
                rs = pst.executeQuery();
                
                jComboBox1.removeAllItems();
                jComboBox1.addItem("Select Customer");
                
                while (rs.next()) {
                    jComboBox1.addItem(rs.getString("fullname"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        } finally {
            closeResources();
        }
    }
    
    /**
     * Sets up automatic calculations and event listeners
     */
    private void setupCalculations() {
        // Add listener to product selection for automatic calculation
        jComboBox2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    calculateTotal();
                }
            }
        });
        
        // Add key listener to quantity field for real-time calculation
        txtQuantity.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateTotal();
            }
        });
    }

    /**
     * Sets up table click listener for row selection
     */
    private void setupTableClickListener() {
        SalesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = SalesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    DefaultTableModel model = (DefaultTableModel) SalesTable.getModel();
                    
                    // Populate fields with selected row data
                    txtId.setText(model.getValueAt(selectedRow, 0).toString());
                    
                    String productName = model.getValueAt(selectedRow, 1).toString();
                    String customerName = model.getValueAt(selectedRow, 2).toString();
                    
                    // Set combo box selections
                    jComboBox2.setSelectedItem(productName);
                    jComboBox1.setSelectedItem(customerName);
                    
                    txtQuantity.setText(model.getValueAt(selectedRow, 3).toString());
                    txtTA.setText(model.getValueAt(selectedRow, 4).toString());
                }
            }
        });
    }

    /**
     * Calculates total amount based on product price and quantity
     */
    private void calculateTotal() {
        try {
            String selectedProduct = (String) jComboBox2.getSelectedItem();
            String quantityText = txtQuantity.getText().trim();
            
            if (selectedProduct != null && !selectedProduct.equals("Select Product") && 
                !quantityText.isEmpty()) {
                
                // Validate quantity is a positive number
                int quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    txtTA.setText("");
                    return;
                }
                
                // Get product price from database
                String sql = "SELECT price FROM productstable WHERE product_name = ?";
                pst = con.prepareStatement(sql);
                pst.setString(1, selectedProduct);
                rs = pst.executeQuery();
                
                if (rs.next()) {
                    double price = rs.getDouble("price");
                    double total = price * quantity;
                    
                    txtTA.setText(String.format("%.2f", total));
                }
            } else {
                txtTA.setText("");
            }
        } catch (NumberFormatException e) {
            txtTA.setText("");
        } catch (Exception e) {
            txtTA.setText("");
            System.err.println("Error calculating total: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    /**
     * Loads all sales data from database into the table
     */
    private void loadSalesData() {
        try {
            if (con != null && !con.isClosed()) {
                String sql = "SELECT s.sale_id, p.product_name, c.fullname, s.quantity, s.amount " +
                            "FROM salestable s " +
                            "JOIN productstable p ON s.prod_id = p.prod_id " +
                            "JOIN customerstable c ON s.id = c.id " +
                            "ORDER BY s.sale_id DESC";
                
                pst = con.prepareStatement(sql);
                rs = pst.executeQuery();
                
                DefaultTableModel model = (DefaultTableModel) SalesTable.getModel();
                model.setRowCount(0); // Clear existing data
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("sale_id"),
                        rs.getString("product_name"),
                        rs.getString("fullname"),
                        rs.getInt("quantity"),
                        String.format("%.2f", rs.getDouble("amount"))
                    };
                    model.addRow(row);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales data: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    /**
     * Gets product ID by product name
     */
    private int getProductId(String productName) throws SQLException {
        String sql = "SELECT prod_id FROM productstable WHERE product_name = ?";
        pst = con.prepareStatement(sql);
        pst.setString(1, productName);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("prod_id");
        }
        throw new SQLException("Product not found: " + productName);
    }

    /**
     * Gets customer ID by customer name
     */
    private int getCustomerId(String customerName) throws SQLException {
        String sql = "SELECT id FROM customerstable WHERE fullname = ?";
        pst = con.prepareStatement(sql);
        pst.setString(1, customerName);
        rs = pst.executeQuery();
        
        if (rs.next()) {
            return rs.getInt("id");
        }
        throw new SQLException("Customer not found: " + customerName);
    }

    /**
     * Validates input fields
     */
    private boolean validateInput() {
        String selectedProduct = (String) jComboBox2.getSelectedItem();
        String selectedCustomer = (String) jComboBox1.getSelectedItem();
        String quantityText = txtQuantity.getText().trim();
        String totalAmountText = txtTA.getText().trim();
        
        if (selectedProduct == null || selectedProduct.equals("Select Product")) {
            JOptionPane.showMessageDialog(this, "Please select a product");
            jComboBox2.requestFocus();
            return false;
        }
        
        if (selectedCustomer == null || selectedCustomer.equals("Select Customer")) {
            JOptionPane.showMessageDialog(this, "Please select a customer");
            jComboBox1.requestFocus();
            return false;
        }
        
        if (quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter quantity");
            txtQuantity.requestFocus();
            return false;
        }
        
        if (totalAmountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Total amount is empty. Please select a product and enter quantity first.");
            return false;
        }
        
        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0");
                txtQuantity.requestFocus();
                return false;
            }
            
            double totalAmount = Double.parseDouble(totalAmountText);
            if (totalAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Total amount must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid quantity and total amount");
            txtQuantity.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Clears all input fields
     */
    private void clearFields() {
        txtId.setText("");
        jComboBox1.setSelectedIndex(0);
        jComboBox2.setSelectedIndex(0);
        txtQuantity.setText("");
        txtTA.setText("");
        txtId.requestFocus();
    }

    /**
     * Closes database resources safely
     */
    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    /**
     * Checks if product has sufficient stock
     */
    private boolean checkStock(String productName, int requestedQuantity) {
        try {
            String sql = "SELECT quantity FROM productstable WHERE product_name = ?";
            pst = con.prepareStatement(sql);
            pst.setString(1, productName);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                int availableStock = rs.getInt("quantity");
                return availableStock >= requestedQuantity;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking stock: " + e.getMessage());
            return false;
        } finally {
            closeResources();
        }
    }

    /**
     * Updates product stock after sale
     */
    private void updateProductStock(String productName, int quantitySold) throws SQLException {
        String sql = "UPDATE productstable SET quantity = quantity - ? WHERE product_name = ?";
        pst = con.prepareStatement(sql);
        pst.setInt(1, quantitySold);
        pst.setString(2, productName);
        pst.executeUpdate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        txtQuantity = new javax.swing.JTextField();
        txtTA = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtSearch = new javax.swing.JLabel();
        updateButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        SalesTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cancel.png"))); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Algerian", 3, 24)); // NOI18N
        jLabel2.setText("manage Sales Form");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Product Registration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        jComboBox1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtQuantity.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        txtQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantityActionPerformed(evt);
            }
        });

        txtTA.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setText("Total Amount");

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Quantity");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel4.setText("Product");

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText("Customer");

        saveButton.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/save.png"))); // NOI18N
        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        deleteButton.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/delete.png"))); // NOI18N
        deleteButton.setText("delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtTA, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(jLabel4)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(txtTA))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(deleteButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(saveButton)))
                .addGap(16, 16, 16))
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Sale Id");

        txtId.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        txtSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/search.png"))); // NOI18N
        txtSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtSearchMouseClicked(evt);
            }
        });

        updateButton.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        updateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/loading-arrow.png"))); // NOI18N
        updateButton.setText("update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        SalesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Sale ID", "Product", "Customer", "Quantity", "Amount"
            }
        ));
        jScrollPane1.setViewportView(SalesTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(168, 168, 168)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtSearch))
                    .addComponent(updateButton))
                .addGap(17, 17, 17)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        // TODO add your handling code here:
            setVisible(false);
            new Dashboard().setVisible(true);
    }//GEN-LAST:event_jLabel1MouseClicked

    private void txtQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantityActionPerformed
        // TODO add your handling code here:
        calculateTotal();
    }//GEN-LAST:event_txtQuantityActionPerformed

    private void txtSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtSearchMouseClicked
        // TODO add your handling code here:
        String saleId = txtId.getText().trim();
        
        if (saleId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Sale ID to search");
            txtId.requestFocus();
            return;
        }
        
        try {
            String sql = "SELECT s.sale_id, p.product_name, c.fullname, s.quantity, s.amount " +
                        "FROM salestable s " +
                        "JOIN productstable p ON s.prod_id = p.prod_id " +
                        "JOIN customerstable c ON s.id = c.id " +
                        "WHERE s.sale_id = ?";
            
            pst = con.prepareStatement(sql);
            pst.setInt(1, Integer.parseInt(saleId));
            rs = pst.executeQuery();
            
            if (rs.next()) {
                jComboBox2.setSelectedItem(rs.getString("product_name"));
                jComboBox1.setSelectedItem(rs.getString("fullname"));
                txtQuantity.setText(String.valueOf(rs.getInt("quantity")));
                txtTA.setText(String.format("%.2f", rs.getDouble("amount")));
                
                JOptionPane.showMessageDialog(this, "Sale record found and loaded!");
            } else {
                JOptionPane.showMessageDialog(this, "No sale record found with ID: " + saleId);
                clearFields();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Sale ID");
            txtId.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching sale: " + e.getMessage());
        } finally {
            closeResources();
        }
    }//GEN-LAST:event_txtSearchMouseClicked

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        // TODO add your handling code here:
        String saleId = txtId.getText().trim();
        
        if (saleId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Sale ID and search for the record first");
            txtId.requestFocus();
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        try {
            String selectedProduct = (String) jComboBox2.getSelectedItem();
            String selectedCustomer = (String) jComboBox1.getSelectedItem();
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            double totalAmount = Double.parseDouble(txtTA.getText());
            
            // Get old quantity for stock adjustment
            String oldQuantitySql = "SELECT quantity FROM salestable WHERE sale_id = ?";
            pst = con.prepareStatement(oldQuantitySql);
            pst.setInt(1, Integer.parseInt(saleId));
            rs = pst.executeQuery();
            
            int oldQuantity = 0;
            if (rs.next()) {
                oldQuantity = rs.getInt("quantity");
            }
            
            int productId = getProductId(selectedProduct);
            int customerId = getCustomerId(selectedCustomer);
            
            // Check stock availability for the difference
            int quantityDifference = quantity - oldQuantity;
            if (quantityDifference > 0 && !checkStock(selectedProduct, quantityDifference)) {
                JOptionPane.showMessageDialog(this, "Insufficient stock for this update!");
                return;
            }
            
            String sql = "UPDATE salestable SET prod_id = ?, id = ?, quantity = ?, amount = ? WHERE sale_id = ?";
            pst = con.prepareStatement(sql);
            pst.setInt(1, productId);
            pst.setInt(2, customerId);
            pst.setInt(3, quantity);
            pst.setDouble(4, totalAmount);
            pst.setInt(5, Integer.parseInt(saleId));
            
            int result = pst.executeUpdate();
            
            if (result > 0) {
                // Adjust stock based on quantity difference
                if (quantityDifference != 0) {
                    String stockSql = "UPDATE productstable SET quantity = quantity - ? WHERE product_name = ?";
                    pst = con.prepareStatement(stockSql);
                    pst.setInt(1, quantityDifference);
                    pst.setString(2, selectedProduct);
                    pst.executeUpdate();
                }
                
                JOptionPane.showMessageDialog(this, "Sale updated successfully!");
                loadSalesData();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update sale. Sale ID may not exist.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid Sale ID and quantity");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating sale: " + e.getMessage());
        } finally {
            closeResources();
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        // TODO add your handling code here:
     if (!validateInput()) {
            return;
        }
        
        try {
            String selectedProduct = (String) jComboBox2.getSelectedItem();
            String selectedCustomer = (String) jComboBox1.getSelectedItem();
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            double totalAmount = Double.parseDouble(txtTA.getText());
            
            // Check stock availability
            if (!checkStock(selectedProduct, quantity)) {
                JOptionPane.showMessageDialog(this, "Insufficient stock! Please check available quantity.");
                return;
            }
            
            int productId = getProductId(selectedProduct);
            int customerId = getCustomerId(selectedCustomer);
            
            String sql = "INSERT INTO salestable (prod_id, id, quantity, amount) VALUES (?, ?, ?, ?)";
            pst = con.prepareStatement(sql);
            pst.setInt(1, productId);
            pst.setInt(2, customerId);
            pst.setInt(3, quantity);
            pst.setDouble(4, totalAmount);
            
            int result = pst.executeUpdate();
            
            if (result > 0) {
                // Update product stock
                updateProductStock(selectedProduct, quantity);
                
                JOptionPane.showMessageDialog(this, "Sale saved successfully!");
                loadSalesData();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save sale");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving sale: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        String saleId = txtId.getText().trim();
        
        if (saleId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Sale ID and search for the record first");
            txtId.requestFocus();
            return;
        }
        
        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this sale record?",
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // First get the sale details to restore stock
            String getSaleSql = "SELECT s.quantity, p.product_name " +
                              "FROM salestable s " +
                              "JOIN productstable p ON s.prod_id = p.prod_id " +
                              "WHERE s.sale_id = ?";
            pst = con.prepareStatement(getSaleSql);
            pst.setInt(1, Integer.parseInt(saleId));
            rs = pst.executeQuery();
            
            if (rs.next()) {
                int quantity = rs.getInt("quantity");
                String productName = rs.getString("product_name");
                
                // Delete the sale
                String deleteSql = "DELETE FROM salestable WHERE sale_id = ?";
                pst = con.prepareStatement(deleteSql);
                pst.setInt(1, Integer.parseInt(saleId));
                
                int result = pst.executeUpdate();
                
                if (result > 0) {
                    // Restore stock
                    String restoreStockSql = "UPDATE productstable SET quantity = quantity + ? WHERE product_name = ?";
                    pst = con.prepareStatement(restoreStockSql);
                    pst.setInt(1, quantity);
                    pst.setString(2, productName);
                    pst.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Sale deleted successfully and stock restored!");
                    loadSalesData();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete sale. Sale ID may not exist.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sale record not found.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Sale ID");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting sale: " + e.getMessage());
        } finally {
            closeResources();
        }

    }                                            

    private void SalesTableMouseClicked(java.awt.event.MouseEvent evt) {                                            
        int selectedRow = SalesTable.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) SalesTable.getModel();
            
            // Populate fields with selected row data
            txtId.setText(model.getValueAt(selectedRow, 0).toString());
            
            String productName = model.getValueAt(selectedRow, 1).toString();
            String customerName = model.getValueAt(selectedRow, 2).toString();
            
            // Set combo box selections
            jComboBox2.setSelectedItem(productName);
            jComboBox1.setSelectedItem(customerName);
            
            txtQuantity.setText(model.getValueAt(selectedRow, 3).toString());
            txtTA.setText(model.getValueAt(selectedRow, 4).toString());
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SalesPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SalesPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SalesPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SalesPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalesPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable SalesTable;
    private javax.swing.JButton deleteButton;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JLabel txtSearch;
    private javax.swing.JTextField txtTA;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables



}
