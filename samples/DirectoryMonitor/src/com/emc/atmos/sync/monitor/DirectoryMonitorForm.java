package com.emc.atmos.sync.monitor;

import com.emc.esu.api.rest.EsuRestApi;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DirectoryMonitorForm extends JFrame implements ActionListener {
    private static final Logger log = Logger.getLogger( DirectoryMonitorForm.class );

    public static void main( String[] args ) {
        new DirectoryMonitorForm();
    }

    private JPanel mainPanel;
    private JTextField atmosPort;
    private JTextField atmosHost;
    private JTextField atmosSecret;
    private JTextField localDirectory;
    private JButton browseButton;
    private JTextField atmosDirectory;
    private JTextField atmosUid;
    private JButton startMonitorButton;
    private JPanel greenLight;
    private JButton stopMonitorButton;

    private DirectoryMonitor monitor;

    public DirectoryMonitorForm() {
        super("AtmosSync Directory Monitor");
        setupUI();
        setMinimumSize( new Dimension( 512, 280 ) );
        setContentPane( mainPanel );
        browseButton.addActionListener( this );
        startMonitorButton.addActionListener( this );
        stopMonitorButton.addActionListener( this );
        monitor = new DirectoryMonitor();
        setVisible( true );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    @Override
    public void actionPerformed( ActionEvent actionEvent ) {

        // browse
        if ( actionEvent.getSource() == browseButton ) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            int returnVal = chooser.showOpenDialog( browseButton );
            if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                File file = chooser.getSelectedFile();
                localDirectory.setText( file.getAbsolutePath() );
            }
        }

        // start monitor
        if ( actionEvent.getSource() == startMonitorButton ) {
            DirectoryMonitorBean bean = new DirectoryMonitorBean();
            try {
                getData( bean );
                monitor.setMonitorBean( bean );
                monitor.startMonitor();
                greenLight.setBackground( Color.GREEN );
            } catch ( NumberFormatException e ) {
                JOptionPane.showMessageDialog( mainPanel, "Atmos port must be a number",
                        "Error", JOptionPane.ERROR_MESSAGE );
            } catch ( Exception e ) {
                JOptionPane.showMessageDialog( mainPanel, e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE );
                log.error( "exception", e );
            }
        }

        // stop monitor
        if ( actionEvent.getSource() == stopMonitorButton ) {
            monitor.stopMonitor();
            greenLight.setBackground( mainPanel.getBackground() );
        }
    }

    public void setData( DirectoryMonitorBean data ) {
        atmosPort.setText( Integer.toString( data.getAtmosPort() ) );
        atmosHost.setText( data.getAtmosHost() );
        atmosUid.setText( data.getAtmosUid() );
        atmosSecret.setText( data.getAtmosSecret() );
        atmosDirectory.setText( data.getAtmosDirectory() );
        localDirectory.setText( data.getLocalDirectory() );
    }

    public void getData( DirectoryMonitorBean data ) {
        data.setAtmosPort( Integer.parseInt( atmosPort.getText() ) );
        data.setAtmosHost( atmosHost.getText() );
        data.setAtmosUid( atmosUid.getText() );
        data.setAtmosSecret( atmosSecret.getText() );
        data.setAtmosDirectory( atmosDirectory.getText() );
        data.setLocalDirectory( localDirectory.getText() );
    }

    private void setupUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout( new GridBagLayout() );
        mainPanel.setMinimumSize( new Dimension( 515, 252 ) );
        final JLabel label1 = new JLabel();
        label1.setEnabled( true );
        label1.setHorizontalAlignment( 4 );
        label1.setText( "Atmos Host:" );
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( label1, gbc );
        atmosPort = new JTextField();
        atmosPort.setText( "80" );
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( atmosPort, gbc );
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment( 4 );
        label2.setText( "Port:" );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( label2, gbc );
        atmosHost = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( atmosHost, gbc );
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment( 4 );
        label3.setText( "UID:" );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( label3, gbc );
        atmosUid = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( atmosUid, gbc );
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment( 4 );
        label4.setText( "Secret Key:" );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( label4, gbc );
        atmosSecret = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( atmosSecret, gbc );
        final JLabel label5 = new JLabel();
        label5.setHorizontalAlignment( 4 );
        label5.setText( "Local Directory:" );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( label5, gbc );
        final JLabel label6 = new JLabel();
        label6.setHorizontalAlignment( 4 );
        label6.setText( "Remote Path:" );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( label6, gbc );
        atmosDirectory = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( atmosDirectory, gbc );
        startMonitorButton = new JButton();
        startMonitorButton.setText( "Start Monitor" );
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( startMonitorButton, gbc );
        greenLight = new JPanel();
        greenLight.setLayout( new GridBagLayout() );
        greenLight.setMaximumSize( new Dimension( 20, 20 ) );
        greenLight.setMinimumSize( new Dimension( 20, 20 ) );
        greenLight.setPreferredSize( new Dimension( 20, 20 ) );
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        mainPanel.add( greenLight, gbc );
        greenLight.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( new Color( -16777216 ) ), null ) );
        stopMonitorButton = new JButton();
        stopMonitorButton.setText( "Stop Monitor" );
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( stopMonitorButton, gbc );
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add( separator1, gbc );
        localDirectory = new JTextField();
        localDirectory.setEnabled( true );
        localDirectory.setEditable( false );
        localDirectory.setMinimumSize( new Dimension( 200, 20 ) );
        localDirectory.setPreferredSize( new Dimension( 200, 20 ) );
        localDirectory.setText( "" );
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( localDirectory, gbc );
        browseButton = new JButton();
        browseButton.setText( "browse" );
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets( 3, 3, 3, 3 );
        mainPanel.add( browseButton, gbc );
        label6.setLabelFor( atmosDirectory );
    }
}
