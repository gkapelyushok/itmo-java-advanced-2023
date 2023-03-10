package info.kgeorgiy.ja.kapelyushok.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return toList(getProperty(students, Student::getFirstName));
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return toList(getProperty(students, Student::getLastName));
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return toList(getProperty(students, Student::getGroup));
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return toList(getProperty(students, student -> student.getFirstName() + " " + student.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return toSet(getProperty(students, Student::getFirstName));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students
                .stream()
                .max(ID_ORDER)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedList(students, ID_ORDER);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedList(students, NAME_ORDER);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findBy(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findBy(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findBy(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students
                .stream()
                .filter(student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }

    private List<Student> sortedList(Collection<Student> students, Comparator<Student> comparator) {
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private <E> Stream<E> getProperty(Collection<Student> students, Function<Student, E> mapper) {
        return students
                .stream()
                .map(mapper);
    }

    private <E> List<E> toList(Stream<E> elements) {
        return elements.collect(Collectors.toList());
    }

    private <E> Set<E> toSet(Stream<E> elements) {
        return elements.collect(Collectors.toCollection(TreeSet::new));
    }

    private <E> List<Student> findBy(Collection<Student> students, Function<Student, E> mapper, E property) {
        return students
                .stream()
                .filter(student -> mapper
                                    .apply(student)
                                    .equals(property))
                .sorted(NAME_ORDER)
                .collect(Collectors.toList());
    }

    //:note: top at class
    private static final Comparator<Student> NAME_ORDER = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparing(Student::getId);

    private static final Comparator<Student> ID_ORDER = Student::compareTo;
}