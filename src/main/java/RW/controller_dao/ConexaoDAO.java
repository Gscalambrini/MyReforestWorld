package RW.controller_dao;

import RW.connection.Conexao;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class ConexaoDAO {

    //private Conexao conexao;
    public void cadastrarUsuario(
            String nome, String email, String senha, String dt_nascimento, String sexo, String cpf, String code) throws SQLException {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement(
                "INSERT INTO users(id, nome, email, senha, dt_nascimento, sexo, cpf, cod_verificacao) values (null,?,?,?,?,?,?,?);");
        p.setString(1, nome);
        p.setString(2, email);
        p.setString(3, senha);
        p.setString(4, dt_nascimento);
        p.setString(5, sexo);
        p.setString(6, cpf);
        p.setString(7, code);
        p.execute();
        p.close();
        conexao.close();
    }

    public void cadastrarEvento(String nome, String local, String data, String descricao, int id_usuario) throws SQLException {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement(
                "INSERT INTO eventos(nome, local, data, descricao, id_usuario) values (?,?,?,?, 1);");
        p.setString(1, nome);
        p.setString(2, local);
        p.setString(3, data);
        p.setString(4, descricao);
        p.execute();
        p.close();
        conexao.close();
    }

    public void cadastrarMuda(String nome_cientifico, String nome_comercial, double valor_muda,
            int cod_estado, int status_muda, String caracteristicas_gerais, String usos_comuns,
            FileInputStream fis, int tamanho, int cod_usuario) throws Exception {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement(
                "INSERT INTO muda (nome_cientifico, nome_comercial, valor_muda, cod_estado, status_muda,"
                + "caracteristicas_gerais, usos_comuns, imagem_muda, cod_usuario) "
                + "values (?,?,?,?,?,?,?,?,?);");

        p.setString(1, nome_cientifico);
        p.setString(2, nome_comercial);
        p.setDouble(3, valor_muda);
        p.setInt(4, cod_estado);
        p.setInt(5, status_muda);
        p.setString(6, caracteristicas_gerais);
        p.setString(7, usos_comuns);
        p.setBlob(8, fis, tamanho);
        p.setInt(9, cod_usuario);
        int confirma = p.executeUpdate();
        if (confirma == 1) {
            JOptionPane.showMessageDialog(null, "Muda cadastrada com sucesso!");
        } else {
            JOptionPane.showMessageDialog(null, "Atenção! Muda não cadastrada. Verifique e tente novamente.");
        }
        p.close();
        conexao.close();
    }

    public Map<String, Object> buscaCadastroMuda(String nome_cientifico) throws Exception {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT * FROM muda WHERE lower(nome_cientifico) = ?;");
        p.setString(1, nome_cientifico);
        var rs = p.executeQuery();
        Map<String, Object> resultadoConsulta = new HashMap<>();
        if (rs.next()) {
            // Extrai os valores do resultado da consulta
            String nomeCientifico = rs.getString("nome_cientifico");
            String nomeComercial = rs.getString("nome_comercial");
            InputStream imagemMudaStream = rs.getBinaryStream("imagem_muda");

            // Lê os bytes da imagem e armazena em um array de bytes
            byte[] imagemMudaBytes = null;
            if (imagemMudaStream != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int bytesRead;
                byte[] data = new byte[4096];
                while ((bytesRead = imagemMudaStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                imagemMudaBytes = buffer.toByteArray();
            }
            resultadoConsulta.put("nome_cientifico", nomeCientifico);
            resultadoConsulta.put("nome_comercial", nomeComercial);
            resultadoConsulta.put("imagem_muda", imagemMudaBytes);
            rs.close();
            p.close();
            conexao.close();
        }
        return resultadoConsulta;
    }

    public boolean existeVerificado(LoginController u) throws Exception {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT * FROM users WHERE email = ? AND  senha = ? AND status_verificacao='Verificado';");
        p.setString(1, u.login);
        p.setString(2, u.senha);
        var rs = p.executeQuery();
        var usuarioExiste = rs.next();
        p.close();
        conexao.close();
        return usuarioExiste;
    }

    public boolean existeNaoVerificado(LoginController u) throws Exception {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT * FROM users WHERE email = ? AND  senha = ? AND status_verificacao='Não Verificado' limit 1;");
        p.setString(1, u.login);
        p.setString(2, u.senha);
        var rs = p.executeQuery();
        var usuarioExiste = rs.next();
        p.close();
        conexao.close();
        return usuarioExiste;
    }

    public void verificacaoUsuarioOk(String email) throws SQLException {
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("UPDATE users SET cod_verificacao='', status_verificacao='Verificado' where email=?");
        p.setString(1, email);
        p.execute();
        p.close();
        conexao.close();
    }

    public boolean verificacaoUsuarioCodigo(String email, String cod_verificacao) throws SQLException {
        boolean verify = false;
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT * FROM users WHERE email=? and cod_verificacao=? limit 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        p.setString(1, email);
        p.setString(2, cod_verificacao);
        ResultSet r = p.executeQuery();
        if (r.first()) {
            verify = true;
        }
        r.close();
        p.close();
        conexao.close();
        return verify;
    }

    public boolean checkCPFDuplicado(String cpf) throws SQLException {
        boolean duplicate = false;
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT id FROM users WHERE CPF=? limit 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        p.setString(1, cpf);
        ResultSet r = p.executeQuery();
        if (r.first()) {
            duplicate = true;
        }
        r.close();
        p.close();
        conexao.close();
        return duplicate;
    }

    public boolean checkEmailDuplicado(String email) throws SQLException {
        boolean duplicate = false;
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT id FROM users WHERE email=? limit 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        p.setString(1, email);
        ResultSet r = p.executeQuery();
        if (r.first()) {
            duplicate = true;
        }
        r.close();
        p.close();
        conexao.close();
        return duplicate;
    }

    public boolean checkMudaDuplicada(String nome_cientifico) throws SQLException {
        boolean duplicate = false;
        var conexao = new Conexao().conectar();
        var p = conexao.prepareStatement("SELECT id FROM muda WHERE nome_cientifico=? limit 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        p.setString(1, nome_cientifico);
        ResultSet r = p.executeQuery();
        if (r.first()) {
            duplicate = true;
        }
        r.close();
        p.close();
        conexao.close();
        return duplicate;
    }
}
