package com.example.data.remote

import com.example.domain.model.*
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("products")
    suspend fun getProducts(): Response<ApiResponse<List<Product>>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ApiResponse<Product>>

    @POST("products")
    suspend fun createProduct(@Body product: Product): Response<ApiResponse<Product>>

    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Response<ApiResponse<Product>>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<ApiResponse<Unit>>

    @GET("categories")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    @POST("categories")
    suspend fun createCategory(@Body category: Category): Response<ApiResponse<Category>>

    @GET("suppliers")
    suspend fun getSuppliers(): Response<ApiResponse<List<Supplier>>>

    @POST("suppliers")
    suspend fun createSupplier(@Body supplier: Supplier): Response<ApiResponse<Supplier>>

    @GET("customers")
    suspend fun getCustomers(): Response<ApiResponse<List<Customer>>>

    @POST("customers")
    suspend fun createCustomer(@Body customer: Customer): Response<ApiResponse<Customer>>

    @GET("warehouses")
    suspend fun getWarehouses(): Response<ApiResponse<List<Warehouse>>>

    @POST("stock/in")
    suspend fun stockIn(@Body movement: StockMovement): Response<ApiResponse<StockMovement>>

    @POST("stock/out")
    suspend fun stockOut(@Body movement: StockMovement): Response<ApiResponse<StockMovement>>

    @POST("stock/adjustment")
    suspend fun adjustStock(@Body adjustment: StockAdjustment): Response<ApiResponse<StockAdjustment>>

    @GET("transactions")
    suspend fun getTransactions(): Response<ApiResponse<List<Transaction>>>

    @POST("transactions")
    suspend fun createTransaction(@Body transaction: Transaction): Response<ApiResponse<Transaction>>

    companion object {
        const val BASE_URL = "https://domain-saya.com/api/"
    }
}
