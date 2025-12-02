package com.utez.edu.mx.viajesbackend.modules.user.DTO;

import java.util.List;

// DTO for paginated user response
public class PaginatedUsersDTO {

    private List<UserDTO> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;

    public PaginatedUsersDTO() {}

    public PaginatedUsersDTO(List<UserDTO> content, long totalElements, int totalPages, int currentPage, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.size = size;
    }

    // Getters and setters
    public List<UserDTO> getContent() {
        return content;
    }

    public void setContent(List<UserDTO> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

