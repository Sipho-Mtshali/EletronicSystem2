import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SalesReportForm extends javax.swing.JFrame {
    private Connection connection;
    private DefaultTableModel tableModel;
    
    public SalesReportForm() {
        initComponents();
        initializeDatabase();
        setupTable();
        loadComboBoxData();
        loadAllSalesData();
    }
private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/ElectronicSystemDb";
            String username = "root";
            String password = "General@123";
            
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void setupTable() {
        String[] columnNames = {"SALE ID", "DATE", "CUSTOMER", "SALES PERSON", "ITEMS", "AMOUNT"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        tblSalesReport.setModel(tableModel);
        
        // Set column widths
        tblSalesReport.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblSalesReport.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblSalesReport.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblSalesReport.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblSalesReport.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblSalesReport.getColumnModel().getColumn(5).setPreferredWidth(100);
    }
    
    private void loadComboBoxData() {
        loadCustomers();
        loadSalesPersons();
    }
    
    private void loadCustomers() {
        try {
            cbCustomer.removeAllItems();
            cbCustomer.addItem("All Customers");
            
            String query = "SELECT id, fullname FROM customerstable ORDER BY fullname";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                cbCustomer.addItem(rs.getString("fullname"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }
    
    private void loadSalesPersons() {
        try {
            cbSalesPerson.removeAllItems();
            cbSalesPerson.addItem("All Sales Staff");
            
            String query = "SELECT id, CONCAT(firstName, ' ', lastName) as fullName FROM users " +
                          "WHERE salesManagement = 1 OR userRole = 'sales clerk' ORDER BY firstName";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                cbSalesPerson.addItem(rs.getString("fullName"));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales persons: " + e.getMessage());
        }
    }
    
    private void loadAllSalesData() {
        try {
            tableModel.setRowCount(0);
            
            String query = "SELECT s.sale_id, s.amount, s.quantity, " +
                          "c.fullname as customer_name, " +
                          "p.product_Name, p.price, " +
                          "u.firstName, u.lastName " +
                          "FROM salestable s " +
                          "JOIN customerstable c ON s.id = c.id " +
                          "JOIN productstable p ON s.prod_id = p.prod_id " +
                          "LEFT JOIN users u ON u.id = s.id " + // Assuming sales person ID is stored somewhere
                          "ORDER BY s.sale_id DESC";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("sale_id"));
                row.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date())); // Use current date as sale date
                row.add(rs.getString("customer_name"));
                row.add((rs.getString("firstName") != null ? rs.getString("firstName") + " " + rs.getString("lastName") : "N/A"));
                row.add(rs.getInt("quantity"));
                row.add(String.format("R%.2f", rs.getDouble("amount")));
                
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading sales data: " + e.getMessage());
        }
    }
    
    private void generateReport() {
        try {
            tableModel.setRowCount(0);
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT s.sale_id, s.amount, s.quantity, ");
            queryBuilder.append("c.fullname as customer_name, ");
            queryBuilder.append("p.product_Name, p.price, ");
            queryBuilder.append("u.firstName, u.lastName ");
            queryBuilder.append("FROM salestable s ");
            queryBuilder.append("JOIN customerstable c ON s.id = c.id ");
            queryBuilder.append("JOIN productstable p ON s.prod_id = p.prod_id ");
            queryBuilder.append("LEFT JOIN users u ON u.id = s.id ");
            queryBuilder.append("WHERE 1=1 ");
            
            // Add filters based on selections
            if (!cbCustomer.getSelectedItem().equals("All Customers")) {
                queryBuilder.append("AND c.fullname = ? ");
            }
            
            if (!cbSalesPerson.getSelectedItem().equals("All Sales Staff")) {
                queryBuilder.append("AND CONCAT(u.firstName, ' ', u.lastName) = ? ");
            }
            
            queryBuilder.append("ORDER BY s.sale_id DESC");
            
            PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
            
            int paramIndex = 1;
            if (!cbCustomer.getSelectedItem().equals("All Customers")) {
                stmt.setString(paramIndex++, cbCustomer.getSelectedItem().toString());
            }
            
            if (!cbSalesPerson.getSelectedItem().equals("All Sales Staff")) {
                stmt.setString(paramIndex++, cbSalesPerson.getSelectedItem().toString());
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("sale_id"));
                row.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                row.add(rs.getString("customer_name"));
                row.add((rs.getString("firstName") != null ? rs.getString("firstName") + " " + rs.getString("lastName") : "N/A"));
                row.add(rs.getInt("quantity"));
                row.add(String.format("R%.2f", rs.getDouble("amount")));
                
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
            JOptionPane.showMessageDialog(this, "Report generated successfully!");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage());
        }
    }
    
    private void exportToExcel() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setSelectedFile(new File("SalesReport.csv"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                
                FileWriter csvWriter = new FileWriter(fileToSave);
                
                // Write headers
                csvWriter.append("SALE ID,DATE,CUSTOMER,SALES PERSON,ITEMS,AMOUNT\n");
                
                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        csvWriter.append(tableModel.getValueAt(i, j).toString());
                        if (j < tableModel.getColumnCount() - 1) {
                            csvWriter.append(",");
                        }
                    }
                    csvWriter.append("\n");
                }
                
                csvWriter.flush();
                csvWriter.close();
                
                JOptionPane.showMessageDialog(this, "Report exported successfully to: " + fileToSave.getPath());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting to Excel: " + e.getMessage());
        }
    }
    
    private void printReport() {
        try {
            boolean complete = tblSalesReport.print(JTable.PrintMode.FIT_WIDTH, 
            null, null, true, null, true);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Print completed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Print operation was cancelled.");
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage());
        }
    }
    
    private void refreshData() {
        // Reset filters
        cbCustomer.setSelectedIndex(0);
        cbSalesPerson.setSelectedIndex(0);
        jDateChooser3.setDate(null);
        jDateChooser4.setDate(null);
        
        // Reload data
        loadAllSalesData();
        
        JOptionPane.showMessageDialog(this, "Data refreshed successfully!");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblSalesReport = new javax.swing.JTable();
        btnExportExcel = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jDateChooser4 = new com.toedter.calendar.JDateChooser();
        jDateChooser3 = new com.toedter.calendar.JDateChooser();
        cbSalesPerson = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        cbCustomer = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        btnGenerateReport = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        tblSalesReport.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "SALE ID", "DATE", "CUSTOMER", "SALES PERSON", "ITEMS", "AMOUNT"
            }
        ));
        jScrollPane1.setViewportView(tblSalesReport);

        btnExportExcel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnExportExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/ExcelLogo.png"))); // NOI18N
        btnExportExcel.setText("Export to Excel");
        btnExportExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportExcelActionPerformed(evt);
            }
        });

        btnPrint.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/PrintLogo.png"))); // NOI18N
        btnPrint.setText("Print Report");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });

        btnRefresh.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/loading-arrow.png"))); // NOI18N
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText("Date From:");

        cbSalesPerson.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        cbSalesPerson.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel5.setText("Sales Person:");

        cbCustomer.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        cbCustomer.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel3.setText("Customer:");

        btnGenerateReport.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnGenerateReport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/reportLogo.png"))); // NOI18N
        btnGenerateReport.setText("Generate Report");
        btnGenerateReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateReportActionPerformed(evt);
            }
        });

        btnClear.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/filterIcon.png"))); // NOI18N
        btnClear.setText("Clear Filters");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel2.setText("Date From:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cbCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel1)
                                    .addGap(18, 18, 18)
                                    .addComponent(jDateChooser4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel3))
                        .addGap(35, 35, 35)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(btnGenerateReport)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnClear)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jDateChooser3, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                        .addComponent(cbSalesPerson, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(50, 50, 50))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jDateChooser4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(47, 47, 47)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(cbSalesPerson, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGenerateReport)
                    .addComponent(btnClear))
                .addGap(33, 33, 33))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel4.setText("Sales Report & Analysis");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cancel.png"))); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(btnExportExcel)
                        .addGap(55, 55, 55)
                        .addComponent(btnPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(186, 186, 186)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6))
                .addGap(28, 28, 28)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExportExcel)
                    .addComponent(btnPrint)
                    .addComponent(btnRefresh))
                .addGap(34, 34, 34)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        // TODO add your handling code here:
        printReport();
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnExportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportExcelActionPerformed
        // TODO add your handling code here:
        exportToExcel();
    }//GEN-LAST:event_btnExportExcelActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        // TODO add your handling code here:
        refreshData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnGenerateReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateReportActionPerformed
        // TODO add your handling code here:
        generateReport();
    }//GEN-LAST:event_btnGenerateReportActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnClearActionPerformed

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        // TODO add your handling code here:
            setVisible(false);
            new Dashboard().setVisible(true);
    }//GEN-LAST:event_jLabel6MouseClicked

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
            java.util.logging.Logger.getLogger(SalesReportForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SalesReportForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SalesReportForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SalesReportForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalesReportForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnExportExcel;
    private javax.swing.JButton btnGenerateReport;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cbCustomer;
    private javax.swing.JComboBox<String> cbSalesPerson;
    private com.toedter.calendar.JDateChooser jDateChooser3;
    private com.toedter.calendar.JDateChooser jDateChooser4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblSalesReport;
    // End of variables declaration//GEN-END:variables
}
