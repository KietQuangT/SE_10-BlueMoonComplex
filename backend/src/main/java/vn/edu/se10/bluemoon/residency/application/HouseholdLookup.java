package vn.edu.se10.bluemoon.residency.application;
import java.util.Optional;
public interface HouseholdLookup { Optional<String> findStatus(long id); }
