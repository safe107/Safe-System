package com.safe.system.project;

import jakarta.persistence.*;
import java.util.*;

@Embeddable
public class ProjectTranslation {
  @Column(name = "title")
  public String title;

  @Column(name = "summary")
  public String summary;

  @Column(name = "description")
  public String description;

  @Column(name = "problem")
  public String problem;

  @Column(name = "solution")
  public String solution;

  @Column(name = "contribution")
  public String contribution;

  @Column(name = "architecture")
  public String architecture;

  @Column(name = "challenges")
  public String challenges;

  @Column(name = "learnings")
  public String learnings;

  @Column(name = "results")
  public String results;
}
