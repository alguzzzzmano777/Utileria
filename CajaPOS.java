import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CajaPOS extends JFrame {
    private static final String DB_URL = "jdbc:postgresql://192.168.10.1:5432/baseDatosProductos";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "admin123";

    private JLabel lblUltimoProducto, lblUltimoDetalle, lblInfoNit, lblQrTexto;
    private JLabel lblFacturaHeader, lblTotalValor, lblFecha, lblHora;
    private JTextArea areaListaProductos;
    private JScrollPane scrollLista;
    private JTextField txtCodigoBarras, txtNitInput;
    private JButton btnConfirmarNit, btnCobrar;

    private String nitActual = "99002";
    private String nombreClienteActual = "SIN NOMBRE";
    private double totalVenta = 0.0;
    private List<ItemCarrito> carrito = new ArrayList<>();

    static class ItemCarrito {
        String nombre;
        int cantidad;
        double precioUnitario;
        double subTotal;

        ItemCarrito(String n, int c, double p) {
            this.nombre = n;
            this.cantidad = c;
            this.precioUnitario = p;
            this.subTotal = c * p;
        }
    }

    public CajaPOS() {
        setTitle("Sistema POS Supermercado - Terminal Caja 07");
        setSize(1024, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(Color.WHITE);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 12, 0));
        panelCentro.setBorder(new EmptyBorder(10, 10, 5, 10));
        panelCentro.setBackground(Color.WHITE);

        // COLUMNA IZQUIERDA
        JPanel panelIzquierdo = new JPanel(new BorderLayout(0, 10));
        panelIzquierdo.setBackground(Color.WHITE);

        JPanel panelSupIzq = new JPanel(new GridLayout(1, 2, 8, 0));
        panelSupIzq.setBorder(new LineBorder(Color.BLACK, 2));
        panelSupIzq.setPreferredSize(new Dimension(480, 120));
        panelSupIzq.setBackground(Color.WHITE);

        JPanel boxProdActual = new JPanel(new GridLayout(3, 1));
        boxProdActual.setBorder(new EmptyBorder(8, 8, 8, 8));
        boxProdActual.setBackground(Color.WHITE);
        lblUltimoProducto = new JLabel("Escanee un producto...");
        lblUltimoProducto.setFont(new Font("Arial", Font.BOLD, 14));
        lblUltimoDetalle = new JLabel("Bs. 0.00 * 0");
        txtCodigoBarras = new JTextField();
        boxProdActual.add(lblUltimoProducto);
        boxProdActual.add(lblUltimoDetalle);
        boxProdActual.add(txtCodigoBarras);

        JPanel boxNit = new JPanel(new BorderLayout(4, 4));
        boxNit.setBorder(new EmptyBorder(6, 6, 6, 6));
        boxNit.setBackground(Color.WHITE);
        lblInfoNit = new JLabel("<html>NIT: <b>99002</b><br>SIN NOMBRE<br><i>¿Es correcto?</i></html>");
        JPanel panelNitControles = new JPanel(new BorderLayout(2, 2));
        txtNitInput = new JTextField("99002");
        btnConfirmarNit = new JButton("Sí / Buscar");
        panelNitControles.add(txtNitInput, BorderLayout.CENTER);
        panelNitControles.add(btnConfirmarNit, BorderLayout.EAST);
        boxNit.add(lblInfoNit, BorderLayout.CENTER);
        boxNit.add(panelNitControles, BorderLayout.SOUTH);

        panelSupIzq.add(boxProdActual);
        panelSupIzq.add(boxNit);

        JPanel panelQr = new JPanel(new BorderLayout());
        panelQr.setBorder(new LineBorder(Color.BLACK, 2));
        panelQr.setBackground(new Color(248, 248, 248));
        lblQrTexto = new JLabel("<html><center><h2>ESPACIO PARA QR DE PAGO</h2><br>Se generará al presionar COBRAR VENTA</center></html>", SwingConstants.CENTER);
        btnCobrar = new JButton("COBRAR Y EMITIR FACTURA");
        btnCobrar.setFont(new Font("Arial", Font.BOLD, 15));
        btnCobrar.setBackground(new Color(34, 139, 34));
        btnCobrar.setForeground(Color.WHITE);
        btnCobrar.setPreferredSize(new Dimension(200, 45));
        panelQr.add(lblQrTexto, BorderLayout.CENTER);
        panelQr.add(btnCobrar, BorderLayout.SOUTH);

        panelIzquierdo.add(panelSupIzq, BorderLayout.NORTH);
        panelIzquierdo.add(panelQr, BorderLayout.CENTER);

        // COLUMNA DERECHA
        JPanel panelDerecho = new JPanel(new BorderLayout(0, 8));
        panelDerecho.setBorder(new LineBorder(Color.BLACK, 2));
        panelDerecho.setBackground(Color.WHITE);

        JPanel panelTicketInterno = new JPanel(new BorderLayout(0, 5));
        panelTicketInterno.setBorder(new EmptyBorder(10, 10, 5, 10));
        panelTicketInterno.setBackground(Color.WHITE);

        lblFacturaHeader = new JLabel("<html><b>FACTURA SUPERMERCADO S.A.</b><br>NIT Cliente: 99002 | Razón Social: SIN NOMBRE<br>------------------------------------------------------------------</html>");
        lblFacturaHeader.setFont(new Font("Monospaced", Font.PLAIN, 12));

        areaListaProductos = new JTextArea();
        areaListaProductos.setEditable(false);
        areaListaProductos.setFont(new Font("Monospaced", Font.PLAIN, 13));
        scrollLista = new JScrollPane(areaListaProductos);
        scrollLista.setBorder(null);

        panelTicketInterno.add(lblFacturaHeader, BorderLayout.NORTH);
        panelTicketInterno.add(scrollLista, BorderLayout.CENTER);

        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setBorder(new CompoundBorder(new EmptyBorder(5, 10, 10, 10), new LineBorder(Color.BLACK, 2)));
        panelTotal.setPreferredSize(new Dimension(450, 65));
        panelTotal.setBackground(Color.WHITE);
        JLabel lblTotTxt = new JLabel("   Precio total");
        lblTotTxt.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalValor = new JLabel("Bs. 0.00   ");
        lblTotalValor.setFont(new Font("Arial", Font.BOLD, 20));
        panelTotal.add(lblTotTxt, BorderLayout.WEST);
        panelTotal.add(lblTotalValor, BorderLayout.EAST);

        panelDerecho.add(panelTicketInterno, BorderLayout.CENTER);
        panelDerecho.add(panelTotal, BorderLayout.SOUTH);

        panelCentro.add(panelIzquierdo);
        panelCentro.add(panelDerecho);
        add(panelCentro, BorderLayout.CENTER);

        // BARRA INFERIOR
        JPanel panelInferior = new JPanel(new GridLayout(1, 5));
        panelInferior.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
        panelInferior.setPreferredSize(new Dimension(1024, 32));
        panelInferior.setBackground(new Color(235, 235, 235));

        lblFecha = new JLabel("Fecha: --/--/----", SwingConstants.CENTER);
        lblHora = new JLabel("Hora: --:--:--", SwingConstants.CENTER);
        panelInferior.add(new JLabel("Caja: 07", SwingConstants.CENTER));
        panelInferior.add(lblFecha);
        panelInferior.add(lblHora);
        panelInferior.add(new JLabel("Usuario: 00132", SwingConstants.CENTER));
        panelInferior.add(new JLabel("Cajero: Rodolfo", SwingConstants.CENTER));
        add(panelInferior, BorderLayout.SOUTH);

        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Date ahora = new Date();
                lblFecha.setText("Fecha: " + new SimpleDateFormat("dd/MM/yyyy").format(ahora));
                lblHora.setText("Hora: " + new SimpleDateFormat("HH:mm:ss").format(ahora));
            }
        }).start();

        txtCodigoBarras.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarYAgregarProducto(txtCodigoBarras.getText().trim());
                txtCodigoBarras.setText("");
            }
        });

        btnConfirmarNit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarOConfirmarNit(txtNitInput.getText().trim());
            }
        });

        btnCobrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                procesarVentaYFactura();
            }
        });
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void buscarYAgregarProducto(String codigo) {
        if (codigo.isEmpty()) return;
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT nombre, precio FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                double precio = rs.getDouble("precio");

                boolean encontrado = false;
                int cantActual = 1;
                for (ItemCarrito item : carrito) {
                    if (item.nombre.equals(nombre)) {
                        item.cantidad++;
                        item.subTotal = item.cantidad * item.precioUnitario;
                        cantActual = item.cantidad;
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    carrito.add(new ItemCarrito(nombre, 1, precio));
                }

                lblUltimoProducto.setText(nombre);
                lblUltimoDetalle.setText(String.format("Bs. %.2f * %d", precio, cantActual));
                actualizarTicket();
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado con código: " + codigo);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión BD: " + ex.getMessage());
        }
    }

    private void buscarOConfirmarNit(String nit) {
        if (nit.isEmpty()) nit = "99002";
        try (Connection conn = conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT nombre_cliente FROM cliente WHERE nit = ?")) {
            ps.setString(1, nit);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombreClienteActual = rs.getString("nombre_cliente");
            } else {
                String nuevoNombre = JOptionPane.showInputDialog(this, "NIT nuevo. Ingrese Razón Social / Apellido:");
                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                    nombreClienteActual = nuevoNombre.trim().toUpperCase();
                    PreparedStatement ins = conn.prepareStatement("INSERT INTO cliente (nit, nombre_cliente) VALUES (?, ?)");
                    ins.setString(1, nit);
                    ins.setString(2, nombreClienteActual);
                    ins.executeUpdate();
                } else {
                    nombreClienteActual = "SIN NOMBRE";
                }
            }
            nitActual = nit;
            lblInfoNit.setText("<html>NIT: <b>" + nitActual + "</b><br>" + nombreClienteActual + "<br><i>[CONFIRMADO]</i></html>");
            lblFacturaHeader.setText("<html><b>FACTURA SUPERMERCADO S.A.</b><br>NIT Cliente: " + nitActual + " | Razón Social: " + nombreClienteActual + "<br>------------------------------------------------------------------</html>");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error buscando NIT: " + ex.getMessage());
        }
    }

    private void actualizarTicket() {
        StringBuilder sb = new StringBuilder();
        totalVenta = 0.0;
        for (ItemCarrito item : carrito) {
            totalVenta += item.subTotal;
            sb.append(String.format("%-26s %2d x %6.2f   Bs. %7.2f\n",
                    item.nombre.length() > 25 ? item.nombre.substring(0, 25) : item.nombre,
                    item.cantidad, item.precioUnitario, item.subTotal));
        }
        areaListaProductos.setText(sb.toString());
        areaListaProductos.setCaretPosition(areaListaProductos.getDocument().getLength());
        lblTotalValor.setText(String.format("Bs. %.2f   ", totalVenta));
    }

    private void procesarVentaYFactura() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos en la lista.");
            return;
        }
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);
            PreparedStatement psVenta = conn.prepareStatement(
                    "INSERT INTO venta (precio_total, metodo_pago, nit_cliente, id_cajero) VALUES (?, 'QR / EFECTIVO', ?, 1) RETURNING id");
            psVenta.setDouble(1, totalVenta);
            psVenta.setString(2, nitActual);
            ResultSet rsVenta = psVenta.executeQuery();
            rsVenta.next();
            int idVenta = rsVenta.getInt(1);

            PreparedStatement psDet = conn.prepareStatement(
                    "INSERT INTO detalle_compra (id_venta, cantidad, producto, precio_producto, sub_precio) VALUES (?, ?, ?, ?, ?)");
            for (ItemCarrito item : carrito) {
                psDet.setInt(1, idVenta);
                psDet.setInt(2, item.cantidad);
                psDet.setString(3, item.nombre);
                psDet.setDouble(4, item.precioUnitario);
                psDet.setDouble(5, item.subTotal);
                psDet.addBatch();
            }
            psDet.executeBatch();

            String cufSimulado = "CUF-" + System.currentTimeMillis() + "-BO";
            PreparedStatement psFac = conn.prepareStatement(
                    "INSERT INTO factura (id_venta, numero_factura, cuf) VALUES (?, ?, ?)");
            psFac.setInt(1, idVenta);
            psFac.setString(2, "FAC-" + (1000 + idVenta));
            psFac.setString(3, cufSimulado);
            psFac.executeUpdate();

            conn.commit();

            lblQrTexto.setText("<html><center><h3>[ QR DE PAGO GENERADO ]</h3>Factura #FAC-" + (1000 + idVenta) + "<br>Total: Bs. " + String.format("%.2f", totalVenta) + "<br>" + cufSimulado + "</center></html>");
            JOptionPane.showMessageDialog(this, "¡Venta #" + idVenta + " registrada y Factura validada correctamente!");

            carrito.clear();
            actualizarTicket();
            lblUltimoProducto.setText("Listo para siguiente cliente...");
            lblUltimoDetalle.setText("Bs. 0.00 * 0");
            txtCodigoBarras.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar venta: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Falta el driver PostgreSQL JDBC");
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CajaPOS().setVisible(true);
            }
        });
    }
}
