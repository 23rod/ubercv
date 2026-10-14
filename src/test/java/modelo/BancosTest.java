package modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BancosTest {

    @Test
    @DisplayName("Dado cada banco de las listas, cuando se valida, entonces se acepta tal como aparece en pantalla")
    void todosLosBancosDeLasListasSonValidos() {
        for (String banco : Bancos.ORIGEN) {
            assertTrue(Bancos.esOrigenValido(banco), banco);
        }
        for (String banco : Bancos.DESTINO) {
            assertTrue(Bancos.esDestinoValido(banco), banco);
            assertTrue(Bancos.esOrigenValido(banco), "todo banco de destino también puede ser de origen: " + banco);
        }
    }

    @Test
    @DisplayName("Dado solo el código de 4 dígitos, cuando se valida, entonces se acepta; si el código no existe o el texto no es un banco, se rechaza")
    void seValidaPorElCodigoDelBanco() {
        assertTrue(Bancos.esOrigenValido("0102"));
        assertTrue(Bancos.esOrigenValido(" 0134 - Banesco "));
        for (String malo : new String[] {null, "", "banco", "0999", "01022", "102", "Banesco"}) {
            assertFalse(Bancos.esOrigenValido(malo), String.valueOf(malo));
            assertFalse(Bancos.esDestinoValido(malo), String.valueOf(malo));
        }
    }

    @Test
    @DisplayName("Dado un banco que solo es de origen, cuando se valida como destino, entonces se rechaza")
    void elDestinoEsUnSubconjuntoDelOrigen() {
        assertTrue(Bancos.esOrigenValido("0108 - Banco Provincial"));
        assertFalse(Bancos.esDestinoValido("0108 - Banco Provincial"));
    }
}
