package com.example.remarket.domain.usecase

//import com.example.remarket.data.repository.ProductRepository
import javax.inject.Inject

/**
 * Caso de uso para crear una nueva publicación de producto.
 *
 * @param repository Implementación de ProductRepository donde se delega la creación.
 */
class CreateProductUseCase @Inject constructor(
    //private val repository: ProductRepository
) {
    /**
     * Ejecuta la operación de creación de producto.
     *
     * @param brand Marca del dispositivo.
     * @param model Modelo del dispositivo.
     * @param storage Capacidad de almacenamiento.
     * @param price Precio en valores numéricos.
     *
     * @throws Exception si la creación falla.
     */
    suspend operator fun invoke(
        brand: String,
        model: String,
        storage: String,
        price: Double
    ) {
        // Aquí delegamos en el repositorio para manejar la lógica de datos
        /*repository.createProduct(
            brand = brand,
            model = model,
            storage = storage,
            price = price
        )*/
    }
}