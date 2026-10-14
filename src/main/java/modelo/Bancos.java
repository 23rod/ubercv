package modelo;

import java.util.List;

/**
 * Bancos del pago móvil. Se valida por el código de 4 dígitos que va al inicio ("0102 - Banco de Venezuela"),
 * así un cambio de redacción en la lista de la pantalla no rompe la validación.
 */
public final class Bancos {

    /** Bancos desde los que el usuario puede hacer su pago móvil. */
    public static final List<String> ORIGEN = List.of(
            "0102 - Banco de Venezuela", "0104 - Banco Venezolano de Crédito", "0105 - Banco Mercantil", "0108 - Banco Provincial",
            "0114 - Bancaribe", "0115 - Banco Exterior", "0128 - Banco Caroní", "0134 - Banesco", "0137 - Banco Sofitasa",
            "0138 - Banco Plaza", "0146 - Banco de la Gente Emprendedora C.A.", "0156 - 100% Banco", "0157 - DelSur Banco Universal",
            "0163 - Banco del Tesoro", "0166 - Banco Agrícola de Venezuela C.A", "0168 - Bancrecer", "0169 - R4, Banco Microfinanciero, C.A.",
            "0171 - Banco Activo", "0172 - Bancamiga", "0173 - Banco Internacional de Desarrollo", "0174 - Banplus",
            "0175 - Banco Digital de los Trabajadores", "0177 - Banco de la FANB", "0178 - N58 Banco Digital",
            "0191 - BNC Banco Nacional de Crédito", "0601 - Instituto Municipal de Crédito Popular");

    /** Bancos donde la institución recibe los pagos. */
    public static final List<String> DESTINO = List.of(
            "0102 - Banco de Venezuela", "0105 - Banco Mercantil", "0134 - Banesco", "0163 - Banco del Tesoro",
            "0175 - Banco Digital de los Trabajadores", "0191 - BNC Banco Nacional de Crédito");

    private Bancos() {
    }

    public static boolean esOrigenValido(String banco) {
        return tieneCodigoDe(banco, ORIGEN);
    }

    public static boolean esDestinoValido(String banco) {
        return tieneCodigoDe(banco, DESTINO);
    }

    private static boolean tieneCodigoDe(String banco, List<String> lista) {
        if (banco == null) {
            return false;
        }
        String texto = banco.trim();
        if (!texto.matches("\\d{4}( - .*)?")) {
            return false;
        }
        String codigo = texto.substring(0, 4);
        for (String conocido : lista) {
            if (conocido.startsWith(codigo)) {
                return true;
            }
        }
        return false;
    }
}
