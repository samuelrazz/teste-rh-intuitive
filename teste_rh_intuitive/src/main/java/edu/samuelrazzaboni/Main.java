package edu.samuelrazzaboni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    private static final String SITE = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos";
    private static final String PASTA_DOWNLOAD = "downloads/";
    private static final String ARQUIVOS_ZIP = "arquivos.zip";

    public static void main(String[] args) {
        try {

            // cria uma pasta de downloads
            Files.createDirectories(Paths.get(PASTA_DOWNLOAD));

            // baixa o html da página
            String html = fetchHtml(SITE);

            // encontra os links usando Regex
            String anexo_I = findPdfLink(html, "Anexo I.");
            String anexo_II = findPdfLink(html, "Anexo II.");

            if (anexo_I != null && anexo_II != null) {

                String file1 = PASTA_DOWNLOAD + "Anexo I.pdf";
                String file2 = PASTA_DOWNLOAD + "Anexo II.pdf";

                downloadFile(anexo_I, file1);
                downloadFile(anexo_II, file2);

                // compactar os pdfs
                zipFiles(new String[] { file1, file2 }, ARQUIVOS_ZIP);

            } else {
                System.out.println("Não foram encontrados os arquivos Anexo I e Anexo II");
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }


    // metodo que baixa o html de um site
    private static String fetchHtml(String urlString) throws IOException {

        StringBuilder conteudo = new StringBuilder();
        URL url = new URL(urlString);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                conteudo.append(linha).append("\n");
            }
        }
        return conteudo.toString();

    }

    // metodo para encontrar um link pdf baseado no nome
    // fonte: "https://stackoverflow.com/questions/46083524/java-regular-expression-to-find-pdf-files"

    private static String findPdfLink(String html, String keyWord){
        String regex = "href=[\"'](.*?" + keyWord + ".*?\\.pdf)[\"']";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }

    // metodo para baixar um arquivo PDF
    // fonte: "https://dicasdeprogramacao.com.br/java-como-fazer-download-de-um-arquivo/"


    private static void downloadFile(String fileURL, String savePath) throws IOException{
        System.out.println("Baixando: " + fileURL);

        URL url = new URL(fileURL);

        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");

        try(InputStream in = conexao.getInputStream();
            FileOutputStream out = new FileOutputStream(savePath)){
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

    }

    // metodo para compactar arquivos em uma pasta zipada
     

    /*
     * fonte: "https://www.devmedia.com.br/compactando-arquivos-em-zip-java/18798"
     *          "https://www.guj.com.br/t/zipar-com-java-transformar-metodo-que-zipa-arquivo-em-metodo-que-zipe-tambem-pastas/132656"
     */

    private static void zipFiles(String[] files, String zipFileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (String filePath : files) {
                File file = new File(filePath);
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

}