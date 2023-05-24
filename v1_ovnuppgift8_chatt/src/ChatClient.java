//Author: Marcus Peterson 2023-05-22
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;



public class ChatClient {
    // Deklaration av en privat instansvariabel 'socket' av typen 'MulticastSocket'.
    // Denna socket kommer att användas för att sända och ta emot meddelanden i multicastgruppen.
    // En socket representerar en enda anslutning mellan två enheter, ofta mellan en klient och en server.
    private MulticastSocket socket;

    private JTextArea textArea;        //textArea är en instansvariabel, skapat från javax.swing biblioteket så att vi kan skapa gui komponenter
    private String name;               //Deklarerar vår instansvariabel (Här skall användarens alias lagras)

    public ChatClient() {
        // Skapar en chatt klient
        try {
            // Skapar ett MulticastSocket-objekt som ska användas för att både sända och ta emot meddelanden
            socket = new MulticastSocket(12540);
            // Ansluter till multicastgruppen och med hjälp av den tilldelade IP-addressen
            socket.joinGroup(InetAddress.getByName("234.235.236.237"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fråga användaren om sitt aliasnamn (Nu tilldelas name instansvariablen ett värde)
        name = JOptionPane.showInputDialog("Enter your chat alias");

        // Skapar grafiska komponenter
        JFrame frame = new JFrame("Chat");
        textArea = new JTextArea();
        JTextField textField = new JTextField();
        JButton exitButton = new JButton("Exit");

        frame.setLayout(new BorderLayout());

        frame.add(textArea, BorderLayout.CENTER);
        frame.add(textField, BorderLayout.SOUTH);
        frame.add(exitButton, BorderLayout.NORTH);

        // Lyssnare för textfält som reagerar på "åtgärder", som vanligtvis utlöses av användarinteraktion, till exempel att trycka på Enter-tangenten i ett textfält.
        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    // Skicka meddelande till multicastgruppen och vem som har skickat meddelandet. När användaren trycker på Enter-tangenten i textField kommer denna metod att anropas.
                    String message = name + ": " + textField.getText();
                    byte[] buffer = message.getBytes(); //Vi konverterar meddelandet till en array av bytes. Detta beror på att nätverkskommunikation sker i form av bytes.

                    //Här skapar vi ett DatagramPacket..
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("234.235.236.237"), 12540);
                    //..med vårt meddelande, storleken på meddelandet, IP-adressen för multicastgruppen och portnumret.

                    socket.send(packet);

                    // Rensa textfältet
                    textField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Lyssnare för exit-knappen
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Lämna multicastgruppen och stänger socket när programmet stängs ned av användaren
                    socket.leaveGroup(InetAddress.getByName("234.235.236.237"));
                    socket.close();

                    // Avslutar programmet vid eventuella exceptions error, så att porten inte är öppen om detta skulle ske (e.g programmet kraschar)
                    System.exit(0);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Skapar och starta lyssnartråden
        Thread listenerThread = new Thread(new Listener(socket, textArea));
        listenerThread.start();

        // Konfigurerar och visar ramen för programmet
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ChatClient();
    }

    class Listener implements Runnable {
    private MulticastSocket socket;
    private JTextArea textArea;

    public Listener(MulticastSocket socket, JTextArea textArea) {
        this.socket = socket;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Väntar på att ett meddelande ska tas emot
                socket.receive(packet);

                // Konverterar meddelandet till en sträng och lägger till det i textarean
                String message = new String(packet.getData(), 0, packet.getLength());
                textArea.append(message + "\n");
            }
        } catch (IOException e) {
            // Om något går fel med in- eller utströmning, skriv ut en felmeddelande
            System.err.println("Error in Listener thread: " + e);
        }
    }
    }


}



