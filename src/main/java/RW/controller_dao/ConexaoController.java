package RW.controller_dao;

import RW.connection.Conexao;
import RW.forms.CadastroTela;
import RW.forms.CadastroEventosTela;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Random;

public class ConexaoController {

    public String cadastroUsuario(CadastroTela view) throws SQLException {

        ConexaoDAO cadastro = new ConexaoDAO();
        String code = generateVerifyCode();
        cadastro.cadastrarUsuario(
            view.getNomeUsuarioTextField().getText(),
            view.getEmailTextField().getText(),
            String.valueOf(view.getSenhaPasswordField().getPassword()),
            view.getIdadeTextField().getText(),
            (String) view.getSexoComboBox().getSelectedItem(),
            view.getCpfTextField().getText(),
            code);
        return code;

    }

    public void cadastroEvento(CadastroEventosTela view) throws SQLException {
            ConexaoDAO cadastro = new ConexaoDAO();
            cadastro.cadastrarEvento(
            view.getNomeEventoTextField().getText(),
            view.getDataEventoTextField().getText(),
            view.getLocalTextField().getText(),
            view.getDescricaoTextArea().getText(),
            1
            );
    }
    
    private String generateVerifyCode() throws SQLException {
        DecimalFormat df = new DecimalFormat("000000");
        Random ran = new Random();
        String code = df.format(ran.nextInt(1000000));  //  Random from 0 to 999999
        while (checkDuplicateCode(code)) {
            code = df.format(ran.nextInt(1000000));
        }
        return code;
    }

    private boolean checkDuplicateCode(String code) throws SQLException {
        boolean duplicate = false;
        var conexao = new Conexao().conectar();
        PreparedStatement p = conexao.prepareStatement("select id from `users` where cod_verificacao=? limit 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        p.setString(1, code);
        ResultSet r = p.executeQuery();
        if (r.first()) {
            duplicate = true;
        }
        r.close();
        p.close();
        return duplicate;
    }
}

