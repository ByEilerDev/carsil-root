package com.carsil.userapi.service;

import com.carsil.userapi.model.Module;
import com.carsil.userapi.model.Product;
import com.carsil.userapi.repository.ModuleRepository;
import com.carsil.userapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
/*
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModuleRepository moduleRepo;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setPrice(150.0);
        testProduct.setQuantity(10);
        testProduct.setReference("REF123");
        testProduct.setBrand("BrandX");
        testProduct.setAssignedDate(LocalDate.now());
        testProduct.setPlantEntryDate(LocalDate.now());
        testProduct.setOp("OP456");
        testProduct.setCampaign("C789");
        testProduct.setType("TypeA");
    }


    @Test
    void getAll_returnsListOfProducts() {

        when(productRepository.findAll()).thenReturn(Collections.singletonList(testProduct));
        List<Product> products = productService.getAll();
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertEquals(1, products.size());
        assertEquals(testProduct.getReference(), products.get(0).getReference());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void create_returnsSavedProduct() {

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        Product createdProduct = productService.create(new Product());
        assertNotNull(createdProduct);
        assertEquals(testProduct.getReference(), createdProduct.getReference());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void create_withExistingOp_throwsException() {
        Product incoming = new Product();
        incoming.setOp("OP-DUPLICATE");
        when(productRepository.existsByOp(incoming.getOp())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.create(incoming));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void delete_callsRepositoryDeleteById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        productService.delete(1L);
        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    void delete_whenProductDoesNotExist_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> productService.delete(99L));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void update_whenProductExists_returnsUpdatedProduct() {
        Product updatedData = new Product();
        updatedData.setReference("UPDATED_REF");
        updatedData.setPrice(99.99);
        updatedData.setOp(testProduct.getOp());
        updatedData.setModule(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedData);

        Product result = productService.update(updatedData, 1L);

        assertNotNull(result);
        assertEquals(updatedData.getReference(), result.getReference());
        assertEquals(updatedData.getPrice(), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_whenProductDoesNotExist_throwsException() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.update(new Product(), 2L));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_withDuplicateOp_throwsException() {
        Product updatedData = new Product();
        updatedData.setOp("OP-DUPLICATE");
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.existsByOpAndIdNot(updatedData.getOp(), 1L)).thenReturn(true);
        assertThrows(DuplicateKeyException.class, () -> productService.update(updatedData, 1L));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void search_returnsMultipleProducts_whenQueryMatchesDifferentFields() {
        Product p1 = new Product();
        p1.setId(401L);
        p1.setReference("NIKE-REF");
        p1.setBrand("BrandZ");
        p1.setOp("OP-NIKE");
        p1.setCampaign("CAMPZ");
        p1.setAssignedDate(LocalDate.now());
        p1.setPlantEntryDate(LocalDate.now());
        p1.setPrice(150.0);
        p1.setQuantity(3);
        p1.setType("TypeX");

        Product p2 = new Product();
        p2.setId(402L);
        p2.setReference("REF123");
        p2.setBrand("Nike");
        p2.setOp("OP123");
        p2.setCampaign("CAMPY");
        p2.setAssignedDate(LocalDate.now());
        p2.setPlantEntryDate(LocalDate.now());
        p2.setPrice(250.0);
        p2.setQuantity(7);
        p2.setType("TypeY");

        when(productRepository.search("nike")).thenReturn(List.of(p1, p2));

        List<Product> results = productService.search("nike");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(p -> "Nike".equals(p.getBrand())));
        assertTrue(results.stream().anyMatch(p -> p.getReference().contains("NIKE")));
    }

    @Test
    void getById_whenProductExists_returnsProduct() {
         when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(testProduct.getReference(), result.get().getReference());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test

    void getById_whenProductDoesNotExist_returnsEmptyOptional() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getById(99L);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(99L);
    }

    // --- Nuevas pruebas para sizeQuantities y module ---

    @Test
    void create_withSizeQuantities_returnsSavedProductAndCorrectQuantity() {
        // Arrange
        Map<String, Integer> sizeMap = new HashMap<>();
        sizeMap.put("S", 5);
        sizeMap.put("M", 5);

        Product productToCreate = new Product();
        productToCreate.setPrice(10.0);
        productToCreate.setAssignedDate(LocalDate.now());
        productToCreate.setPlantEntryDate(LocalDate.now());
        productToCreate.setReference("REF-SIZE-TEST");
        productToCreate.setBrand("BrandZ");
        productToCreate.setOp("OP-SIZE-TEST");
        productToCreate.setCampaign("CAMP-SIZE");
        productToCreate.setType("TypeB");
        productToCreate.setSizeQuantities(sizeMap);
        productToCreate.setQuantity(10); // Simula el comportamiento del @PrePersist

        when(productRepository.existsByOp(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(productToCreate);

        // Act
        Product createdProduct = productService.create(productToCreate);

        // Assert
        assertNotNull(createdProduct);
        assertEquals(10, createdProduct.getQuantity());
        assertEquals(sizeMap, createdProduct.getSizeQuantities());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_withSizeQuantities_returnsUpdatedProduct() {
        // Arrange
        Map<String, Integer> updatedSizeMap = new HashMap<>();
        updatedSizeMap.put("L", 3);
        updatedSizeMap.put("XL", 7);

        Product updatedData = new Product();
        updatedData.setReference("UPDATED_REF");
        updatedData.setPrice(99.99);
        updatedData.setOp(testProduct.getOp());
        updatedData.setSizeQuantities(updatedSizeMap);

        // Simula la cantidad actualizada
        testProduct.setQuantity(10);
        testProduct.setSizeQuantities(updatedSizeMap);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.update(updatedData, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(updatedSizeMap, result.getSizeQuantities());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void create_withExistingModule_returnsSavedProduct() {
        // Arrange
        Long moduleId = 10L;
        Module mockModule = new Module();
        mockModule.setId(moduleId);

        Product productWithModule = new Product();
        productWithModule.setModule(mockModule);
        productWithModule.setPrice(50.0);
        productWithModule.setAssignedDate(LocalDate.now());
        productWithModule.setPlantEntryDate(LocalDate.now());
        productWithModule.setReference("REF-MOD-TEST");
        productWithModule.setBrand("ModuleBrand");
        productWithModule.setOp("OP-MOD-TEST");
        productWithModule.setCampaign("CAMP-MOD");
        productWithModule.setType("TypeC");

        when(moduleRepo.findById(moduleId)).thenReturn(Optional.of(mockModule));
        when(productRepository.save(any(Product.class))).thenReturn(productWithModule);
        when(productRepository.existsByOp(anyString())).thenReturn(false);

        // Act
        Product createdProduct = productService.create(productWithModule);

        // Assert
        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getModule());
        assertEquals(moduleId, createdProduct.getModule().getId());
        verify(moduleRepo, times(1)).findById(moduleId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void create_withNonExistentModule_throwsException() {
        // Arrange
        Long nonExistentModuleId = 99L;
        Product productWithModule = new Product();
        Module nonExistentModule = new Module();
        nonExistentModule.setId(nonExistentModuleId);
        productWithModule.setModule(nonExistentModule);
        productWithModule.setPrice(50.0);
        productWithModule.setAssignedDate(LocalDate.now());
        productWithModule.setPlantEntryDate(LocalDate.now());
        productWithModule.setReference("REF-MOD-FAIL");
        productWithModule.setBrand("ModuleBrand");
        productWithModule.setOp("OP-MOD-FAIL");
        productWithModule.setCampaign("CAMP-MOD-FAIL");
        productWithModule.setType("TypeD");

        when(moduleRepo.findById(nonExistentModuleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.create(productWithModule));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void update_withModule_returnsUpdatedProduct() {
        // Arrange
        Long newModuleId = 20L;
        Module newModule = new Module();
        newModule.setId(newModuleId);

        Product updatedData = new Product();
        updatedData.setModule(newModule);
        updatedData.setReference("UPDATED_REF");
        updatedData.setOp(testProduct.getOp());

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(moduleRepo.findById(newModuleId)).thenReturn(Optional.of(newModule));
        when(productRepository.save(any(Product.class))).thenReturn(updatedData);

        // Act
        Product result = productService.update(updatedData, 1L);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getModule());
        assertEquals(newModuleId, result.getModule().getId());
        verify(productRepository, times(1)).findById(1L);
        verify(moduleRepo, times(1)).findById(newModuleId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void update_withNonExistentModule_throwsException() {
        // Arrange
        Long nonExistentModuleId = 99L;
        Product updatedData = new Product();
        Module nonExistentModule = new Module();
        nonExistentModule.setId(nonExistentModuleId);
        updatedData.setModule(nonExistentModule);
        updatedData.setReference("UPDATED_REF");
        updatedData.setOp(testProduct.getOp());

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(moduleRepo.findById(nonExistentModuleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> productService.update(updatedData, 1L));
        verify(productRepository, times(1)).findById(1L);
        verify(moduleRepo, times(1)).findById(nonExistentModuleId);
        verify(productRepository, never()).save(any(Product.class));
    }*/
}