
import java.awt.HeadlessException;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class InventoryPage extends javax.swing.JFrame {
    private String currentUserRole; // Track user role for access control
    private Connection connection;
    
   public InventoryPage() {
        this("admin"); // Default to admin for testing
    }
    
    public InventoryPage(String userRole) {
        this.currentUserRole = userRole;
        initComponents();
        initializeDatabase();
        loadTableData();
        setupTableSelection();
        configureAccessControl();
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
        closeConnection();
    }
});

    }
// Initialize database connection
    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/ElectronicSystemDb", 
                "root", 
                "Zethembe@123456789"
            );
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + ex.getMessage());
        }
    }

    // Configure access control based on user role
    private void configureAccessControl() {
        if ("CLERK".equals(currentUserRole)) {
            // Sales clerk has view-only access to inventory
            buttonAdd.setEnabled(false);
            buttonUpdate.setEnabled(false);
            buttonDelete.setEnabled(false);
            
            // Make text fields read-only
            txtName.setEditable(false);
            txtPrice.setEditable(false);
            txtStock.setEditable(false);
            txtId.setEditable(false);
            
            // Keep search functionality
            txtSearch.setEnabled(true);
        }
    }

    // Method to load data from database into table with error handling
    private void loadTableData() {
        DefaultTableModel model = (DefaultTableModel) ProductTable.getModel();
        model.setRowCount(0);
        
        String sql = "SELECT prod_id, product_name, price, stock FROM productstable ORDER BY prod_id";
        
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("prod_id"),
                    rs.getString("product_name"),
                    String.format("%.2f", rs.getDouble("price")), // Format price
                    rs.getInt("stock")
                };
                model.addRow(row);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Enhanced table selection with proper event handling
    private void setupTableSelection() {
        ProductTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        ListSelectionModel selectionModel = ProductTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = ProductTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        populateFieldsFromTable(selectedRow);
                    }
                }
            }
        });
    }
    
    // Populate form fields from selected table row
    private void populateFieldsFromTable(int row) {
        DefaultTableModel model = (DefaultTableModel) ProductTable.getModel();
        
        txtId.setText(model.getValueAt(row, 0).toString());
        txtName.setText(model.getValueAt(row, 1).toString());
        txtPrice.setText(model.getValueAt(row, 2).toString());
        txtStock.setText(model.getValueAt(row, 3).toString());
    }
    
    // Enhanced validation method
    private boolean validateInput() {
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String stockStr = txtStock.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name cannot be empty!");
            txtName.requestFocus();
            return false;
        }
        
        if (priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Price cannot be empty!");
            txtPrice.requestFocus();
            return false;
        }
        
        if (stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Stock quantity cannot be empty!");
            txtStock.requestFocus();
            return false;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                JOptionPane.showMessageDialog(this, "Price cannot be negative!");
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price!");
            txtPrice.requestFocus();
            return false;
        }
        
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "Stock cannot be negative!");
                txtStock.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid stock quantity!");
            txtStock.requestFocus();
            return false;
        }
        
        return true;
    }
    
    // Helper method to clear all text fields
    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        ProductTable.clearSelection();
    }

    // Enhanced search functionality
    private void performSearch() {
        String searchId = txtId.getText().trim();
        
        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Product ID to search!");
            txtId.requestFocus();
            return;
        }
        
        String sql = "SELECT * FROM productstable WHERE prod_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, searchId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                txtName.setText(rs.getString("product_name"));
                txtPrice.setText(String.format("%.2f", rs.getDouble("price")));
                txtStock.setText(String.valueOf(rs.getInt("stock")));
                
                // Highlight the row in table
                highlightRowInTable(searchId);
            } else {
                JOptionPane.showMessageDialog(this, "Product not found with ID: " + searchId);
                txtName.setText("");
                txtPrice.setText("");
                txtStock.setText("");
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Highlight row in table based on product ID
    private void highlightRowInTable(String productId) {
        DefaultTableModel model = (DefaultTableModel) ProductTable.getModel();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            if (productId.equals(model.getValueAt(i, 0).toString())) {
                ProductTable.setRowSelectionInterval(i, i);
                ProductTable.scrollRectToVisible(ProductTable.getCellRect(i, 0, true));
                break;
            }
        }
    }

    // Enhanced Add functionality
 private void addProduct() {
    if (!"admin".equals(currentUserRole)) {
        JOptionPane.showMessageDialog(this, "You do not have permission to add products.");
        return;
    }

    if (!validateInput()) return;

    String name = txtName.getText().trim();
    double price = Double.parseDouble(txtPrice.getText().trim());
    int stock = Integer.parseInt(txtStock.getText().trim());

    String sql = "INSERT INTO productstable(product_name, price, stock) VALUES(?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql)) {
        ps.setString(1, name);
        ps.setDouble(2, price);
        ps.setInt(3, stock);

        int result = ps.executeUpdate();

        if (result > 0) {
            JOptionPane.showMessageDialog(this, "Product added successfully!");
            clearFields();
            loadTableData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add product!");
        }

    } catch (SQLException ex) {
        if (ex.getErrorCode() == 1062) {
            JOptionPane.showMessageDialog(this, "Product already exists!");
        } else {
            JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    // Enhanced Update functionality
    private void updateProduct() {
        String id = txtId.getText().trim();
        
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a product to update!");
            return;
        }
        
        if (!validateInput()) return;
        
        String name = txtName.getText().trim();
        double price = Double.parseDouble(txtPrice.getText().trim());
        int stock = Integer.parseInt(txtStock.getText().trim());
        
        String sql = "UPDATE productstable SET product_name = ?, price = ?, stock = ? WHERE prod_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, stock);
            ps.setString(4, id);
            
            int result = ps.executeUpdate();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                loadTableData();
                highlightRowInTable(id); // Keep the updated row selected
            } else {
                JOptionPane.showMessageDialog(this, "No product found with ID: " + id);
            }
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Enhanced Delete functionality
    private void deleteProduct() {
        String id = txtId.getText().trim();
        
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete!");
            return;
        }
        
        String productName = txtName.getText().trim();
        String message = "Are you sure you want to delete:\n" + 
                        "ID: " + id + "\n" + 
                        "Product: " + productName + "?";
        
        int confirm = JOptionPane.showConfirmDialog(this, message, 
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM productstable WHERE prod_id = ?";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, id);
                
                int result = ps.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    clearFields();
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "No product found with ID: " + id);
                }
                
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 1451) { // Foreign key constraint
                    JOptionPane.showMessageDialog(this, 
                        "Cannot delete product: It may be referenced in sales records!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage(), 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Clean up database connection
   // Clean up database connection
    private void closeConnection() {
    try {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error closing database connection: " + ex.getMessage(),
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}


    // Event handlers (replace your existing methods)


    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        buttonAdd = new javax.swing.JButton();
        buttonUpdate = new javax.swing.JButton();
        buttonDelete = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        txtSearch = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ProductTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cancel.png"))); // NOI18N
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Algerian", 3, 24)); // NOI18N
        jLabel2.setText("manage inventory Form");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Product details"));

        buttonAdd.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        buttonAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/plus.png"))); // NOI18N
        buttonAdd.setText("Add");
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        buttonUpdate.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        buttonUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/loading-arrow.png"))); // NOI18N
        buttonUpdate.setText("Update");
        buttonUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonUpdateActionPerformed(evt);
            }
        });

        buttonDelete.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        buttonDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/delete.png"))); // NOI18N
        buttonDelete.setText("Delete");
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Product Name");

        txtName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Price");

        txtPrice.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel6.setText("quantity stock");

        txtStock.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel7.setText("Product ID");

        txtId.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N

        txtSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/search.png"))); // NOI18N
        txtSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtSearchMouseClicked(evt);
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
                        .addComponent(buttonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(67, 67, 67)
                        .addComponent(buttonUpdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                        .addComponent(buttonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(txtId)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(129, 129, 129))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel7))
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtSearch))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(23, 23, 23)
                        .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(23, 23, 23)
                        .addComponent(txtStock, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonAdd)
                    .addComponent(buttonUpdate)
                    .addComponent(buttonDelete))
                .addContainerGap())
        );

        ProductTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product ID", "Product Name", "Price", "Quantity stock"
            }
        ));
        jScrollPane1.setViewportView(ProductTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        // TODO add your handling code here:
        closeConnection();
        setVisible(false);
        new Dashboard().setVisible(true);
    }//GEN-LAST:event_jLabel1MouseClicked

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
    addProduct();
    }//GEN-LAST:event_buttonAddActionPerformed

    private void txtSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtSearchMouseClicked
      // TODO add your handling code here:
       performSearch();
    }//GEN-LAST:event_txtSearchMouseClicked

    private void buttonUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUpdateActionPerformed
      // TODO add your handling code here:
       updateProduct();
    }//GEN-LAST:event_buttonUpdateActionPerformed

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteActionPerformed
        // TODO add your handling code here:
       deleteProduct();
    }//GEN-LAST:event_buttonDeleteActionPerformed

    // Override window closing to clean up resources
    @Override
    public void dispose() {
        closeConnection();
        super.dispose();
    }
    
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
            java.util.logging.Logger.getLogger(InventoryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InventoryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InventoryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InventoryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new InventoryPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable ProductTable;
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonDelete;
    private javax.swing.JButton buttonUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JLabel txtSearch;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}
