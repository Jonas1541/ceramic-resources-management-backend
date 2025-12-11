package com.jonasdurau.ceramicmanagement.shared.validation;

public interface DeletionValidator<ID> {
    /**
     * Valida se a entidade com o ID fornecido pode ser deletada.
     * @throws RuntimeException (ex: ResourceDeletionException) se a deleção não for permitida.
     */
    void validate(ID id);
}
