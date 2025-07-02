import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
public class ViewUsersForm extends javax.swing.JFrame {
private boolean listenerAdded = false; // Flag to prevent multiple listeners
    public ViewUsersForm() {
        initComponents();
        loadUsers();
        setupTableListener(); // Separate method for setting up listener
        
        tblUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = tblUsers.columnAtPoint(evt.getPoint());
                if (column == 0) {
                    JOptionPane.showMessageDialog(null, "You cannot edit the User ID field.");
                }
            }
        });
    }

 private void setupTableListener() {
        if (!listenerAdded) {
            tblUsers.getModel().addTableModelListener(e -> {
                if (e.getColumn() == 0) {
                    JOptionPane.showMessageDialog(this, "User ID cannot be edited.");
                    SwingUtilities.invokeLater(() -> loadUsers()); // Use invokeLater to avoid conflicts
                }
            });
            listenerAdded = true;
        }
    }

    private void loadUsers() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[][] {},
            new String[] {"User ID", "Firstname", "Lastname", "Username", "Email", "User Role", "Contact"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make only the ID column uneditable
                return column != 0;
            }
        };

        tblUsers.setModel(model);

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ElectronicSystemDb",
                "root", "General@123");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("userRole"),
                    rs.getString("phoneNumber")
                });
            }

            conn.close();
        } 
        catch (SQLException e){
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
        
        // Re-setup the listener for the new model
        setupTableListener();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        jLabel1.setText("View All Users");

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "User ID", "Firstname", "Lastname", "Username", "Email", "User Role", "Contact"
            }
        ));
        jScrollPane1.setViewportView(tblUsers);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/cancel.png"))); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/loading-arrow.png"))); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
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
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 703, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(239, 239, 239)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(btnUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDelete)
                .addGap(100, 100, 100))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        // TODO add your handling code here:
            setVisible(false);
            new Dashboard().setVisible(true);
    }//GEN-LAST:event_jLabel2MouseClicked

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int selectedRow = tblUsers.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to update.");
            return;
        }

        try {
            int userId = (int) tblUsers.getValueAt(selectedRow, 0);
            String firstName = tblUsers.getValueAt(selectedRow, 1) != null ? 
                tblUsers.getValueAt(selectedRow, 1).toString() : "";
            String lastName = tblUsers.getValueAt(selectedRow, 2) != null ? 
                tblUsers.getValueAt(selectedRow, 2).toString() : "";
            String username = tblUsers.getValueAt(selectedRow, 3) != null ? 
                tblUsers.getValueAt(selectedRow, 3).toString() : "";
            String email = tblUsers.getValueAt(selectedRow, 4) != null ? 
                tblUsers.getValueAt(selectedRow, 4).toString() : "";
            String userRole = tblUsers.getValueAt(selectedRow, 5) != null ? 
                tblUsers.getValueAt(selectedRow, 5).toString() : "";
            String phoneNumber = tblUsers.getValueAt(selectedRow, 6) != null ? 
                tblUsers.getValueAt(selectedRow, 6).toString() : "";

            // Debug output - remove this after fixing
            System.out.println("Debug Info:");
            System.out.println("User ID: " + userId);
            System.out.println("Phone Number from table: '" + phoneNumber + "'");
            System.out.println("Phone Number length: " + phoneNumber.length());

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ElectronicSystemDb",
                "root", "General@123");

            // Try different possible column names for phone number
            String sql = "UPDATE users SET firstName=?, lastName=?, username=?, email=?, userRole=?, phoneNumber=? WHERE id=?";
            
            // Alternative column names to try if phoneNumber doesn't work:
            // phone, contact, contactNumber, phone_number, contact_number
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, username);
            stmt.setString(4, email);
            stmt.setString(5, userRole);
            stmt.setString(6, phoneNumber);
            stmt.setInt(7, userId);
            
            int rowsAffected = stmt.executeUpdate();
            
            // Additional debug: Check what's actually in the database
            String checkSql = "SELECT phoneNumber FROM users WHERE id=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("Phone number in DB after update: '" + rs.getString("phoneNumber") + "'");
            }
            
            conn.close();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "User updated successfully.");
                loadUsers(); // refresh table
            } else {
                JOptionPane.showMessageDialog(this, "No user was updated. Please check the user ID.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage());
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = tblUsers.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        int userId = (int) tblUsers.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user ID " + userId + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ElectronicSystemDb",
                    "root", "General@123");

                String sql = "DELETE FROM users WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                conn.close();

                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                loadUsers(); // refresh table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

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
            java.util.logging.Logger.getLogger(ViewUsersForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ViewUsersForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ViewUsersForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ViewUsersForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ViewUsersForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblUsers;
    // End of variables declaration//GEN-END:variables
}
