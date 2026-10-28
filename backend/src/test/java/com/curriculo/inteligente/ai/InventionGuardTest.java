package com.curriculo.inteligente.ai;

import com.curriculo.inteligente.common.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventionGuardTest {

    @Test
    void acceptsARewriteWithoutNewNumbers() {
        assertDoesNotThrow(() -> InventionGuard.assertFaithful(
                "Eu ficava vendo os pedidos.",
                "Acompanhava pedidos e verificava pendências."));
    }

    @Test
    void keepsTheSentencesThatDoNotInventNumbers() {
        String kept = InventionGuard.keepFaithful(
                "Reposição de mercadorias.",
                "Mantive a organização das mercadorias. Atuei por 12 anos no setor.");
        org.junit.jupiter.api.Assertions.assertEquals("Mantive a organização das mercadorias.", kept);
    }

    @Test
    void rejectsANewPercentageOrYear() {
        assertThrows(ApiException.class, () -> InventionGuard.assertFaithful(
                "Eu via os pedidos.", "Reduzi 30% do tempo."));
        assertThrows(ApiException.class, () -> InventionGuard.assertFaithful(
                "Trabalhei na padaria.", "Trabalhei na padaria em 2019."));
    }
}
