import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Cliente{
    static Scanner scanner = new Scanner(System.in);

    private String nome;

    public Cliente() throws IOException {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // Setando porta e endereço do socket avisos

    private static final String GRUPO_AVISOS = "224.0.0.1";
    private static final int PORTA_AVISOS = 4551;

    // Setando porta e endereço do socket chat
    private static final String GRUPO_CHAT = "224.0.0.0";
    private static final int PORTA_CHAT = 8300;

    private static int status = 3; // 1 - Avisos, 2 - Chat, 0 - Sair

    public static void main(String[] args) throws IOException {
        System.out.println("1 - Entrar");
        System.out.println("0 - Sair");

        System.out.print("Digite a opção: ");
        int opcao = scanner.nextInt();

        if (opcao == 0){
            System.out.println("Saindo...");
            System.exit(0);
        }
        else if(opcao == 1){
            System.out.print("Digite seu nome de usuário: ");
            scanner.nextLine();
            String nome = scanner.nextLine();

            Cliente cliente = new Cliente();

            cliente.setNome(nome);

            System.out.println("Bem vindo! " + cliente.getNome());
            System.out.println("1 - Novidades");
            System.out.println("2 - Chat");
            System.out.println("0 - Sair");

            System.out.print("Digite a opção: ");
            int opcaoMenu = scanner.nextInt();

            if (opcaoMenu == 0){
                System.out.println("Saindo...");
                System.exit(0);
            }

            else if (opcaoMenu == 1) {
                System.out.println("---- TÓPICO DE NOVIDADES ----");
                Thread threadReceberPacote = new Thread(new receberPacoteAviso());
                threadReceberPacote.start();
                System.out.println("Para sair do tópico de novidades digite [SAIR]");

                while (status != 0){
                    String sair = scanner.nextLine();

                    if (sair.equals("SAIR")){
                        System.out.println("Saindo...");
                        status = 0;
                        threadReceberPacote.stop();
                        System.exit(1);
                    }
                }
            }

            else if (opcaoMenu == 2) {
                System.out.println("---- CHAT / Digite [SAIR] para sair do chat ----");

                InetAddress grupo = InetAddress.getByName("224.0.0.0");
                MulticastSocket socket = new MulticastSocket(PORTA_CHAT);
                socket.joinGroup(InetAddress.getByName(GRUPO_CHAT));

                byte[] envio;
                Thread threadReceberPacote = new Thread(new receberPacoteChat());
                threadReceberPacote.start();



                String entrada = cliente.getNome() + " entrou no chat";
                envio = entrada.trim().getBytes();
                DatagramPacket pacote_entrada = new DatagramPacket(envio, envio.length, grupo, PORTA_CHAT);
                socket.send(pacote_entrada);

                try {
                    while (status != 0) {
                        byte[] buffer;
                        String mensagem = scanner.nextLine().trim();

                        if (mensagem.isEmpty()) {
                            continue;
                        }

                        if (mensagem.equals("SAIR")){
                            String saida = cliente.getNome() + " saiu do chat";
                            buffer = saida.trim().getBytes();

                            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupo, PORTA_CHAT);
                            socket.send(pacote);
                            socket.leaveGroup(InetAddress.getByName(GRUPO_CHAT));
                            System.exit(1);
                        }

                        Date dataHoraAtual = new Date();
                        SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                        String dataHoraFormatada = formatoDataHora.format(dataHoraAtual);

                        String mensagemFormatada = "[" + dataHoraFormatada + "] " + cliente.getNome() + " : " + mensagem;

                        envio = mensagemFormatada.trim().getBytes();

                        DatagramPacket pacote = new DatagramPacket(envio, envio.length, grupo, PORTA_CHAT);
                        socket.send(pacote);

                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    socket.close();
                    System.exit(1);
                }
            }
        }
    }

    static class receberPacoteAviso implements Runnable{
        @Override
        public void run() {
            try {
                MulticastSocket socket = new MulticastSocket(PORTA_AVISOS);
                socket.joinGroup(InetAddress.getByName(GRUPO_AVISOS));

                while (status != 0) {
                        byte[] buffer = new byte[1024];
                        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                        socket.receive(pacote);

                        String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                        System.out.println(mensagem);
                }

                socket.leaveGroup(InetAddress.getByName(GRUPO_AVISOS));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class receberPacoteChat implements Runnable{
        @Override
        public void run() {
            try {
                MulticastSocket socket = new MulticastSocket(PORTA_CHAT);
                socket.joinGroup(InetAddress.getByName(GRUPO_CHAT));

                while (status != 0) {

                    byte[] buffer = new byte[1024];
                    DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                    socket.receive(pacote);

                    String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                    System.out.println(mensagem);
                }

                socket.leaveGroup(InetAddress.getByName(GRUPO_CHAT));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

