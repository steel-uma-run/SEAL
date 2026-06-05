package seal.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "lecturers")
public class Lecturer extends User {}
