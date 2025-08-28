package uk.co.devinity.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.devinity.entities.Entry;
import uk.co.devinity.entities.User;
import uk.co.devinity.repositories.EntryRepository;
import uk.co.devinity.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EntryController.class)
class EntryControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EntryRepository entryRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setup() {
        alice = new User();
        alice.setId(1L);
        alice.setName("Alice");
        alice.setBmr(1500);

        bob = new User();
        bob.setId(2L);
        bob.setName("Bob");
        bob.setBmr(1800);
    }

    @Test
    void index_shouldRenderUsers() throws Exception {
        given(userRepository.findAll()).willReturn(List.of(alice, bob));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andExpect(view().name("index"));
    }

    @Test
    void entryForm_shouldPrefillTodayAndBindUser() throws Exception {
        given(userRepository.findById(1L)).willReturn(Optional.of(alice));

        mvc.perform(get("/entry/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("entry"))
                .andExpect(view().name("entry-form"));
    }

    @Test
    void submitEntry_shouldAttachUserAndRedirect() throws Exception {
        given(userRepository.findById(1L)).willReturn(Optional.of(alice));

        mvc.perform(post("/entry/1")
                        .param("date", LocalDate.now().toString())
                        .param("caloriesConsumed", "2000")
                        .param("caloriesBurnt", "500")
                        .param("weight", "70.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/combined-dashboard"));

        ArgumentCaptor<Entry> captor = ArgumentCaptor.forClass(Entry.class);
        verify(entryRepository).save(captor.capture());
        Entry saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(alice);
        assertThat(saved.getCaloriesConsumed()).isEqualTo(2000);
        assertThat(saved.getCaloriesBurnt()).isEqualTo(500);
        assertThat(saved.getWeight()).isEqualTo(70.5);
    }

    @Test
    void combinedDashboard_shouldBuildModel() throws Exception {
        given(userRepository.findAll()).willReturn(List.of(alice, bob));

        Entry e1 = new Entry();
        e1.setUser(alice);
        e1.setDate(LocalDate.of(2025, 8, 25));
        e1.setCaloriesConsumed(2000);
        e1.setCaloriesBurnt(450);
        e1.setWeight(70.2);

        Entry e2 = new Entry();
        e2.setUser(alice);
        e2.setDate(LocalDate.of(2025, 8, 26));
        e2.setCaloriesConsumed(2100);
        e2.setCaloriesBurnt(500);
        e2.setWeight(70.0);

        given(entryRepository.findByUserOrderByDateAsc(alice)).willReturn(List.of(e1, e2));
        given(entryRepository.findByUserOrderByDateAsc(bob)).willReturn(List.of());

        mvc.perform(get("/combined-dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("userEntriesMap"))
                .andExpect(view().name("combined-dashboard"));
    }
}
