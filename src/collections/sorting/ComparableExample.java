package collections.sorting;

import java.util.*;

/**
 * Comparable defines the natural ordering of a class.
 * Used by default in Collections.sort, Arrays.sort, TreeSet, TreeMap.
 */
public class ComparableExample {

    public static void main(String[] args) {
        List<Employee> list = new ArrayList<>(List.of(
            new Employee("Carol", 70_000),
            new Employee("Alice", 90_000),
            new Employee("Bob",   50_000)
        ));

        // Sorts using compareTo — by salary ascending
        Collections.sort(list);
        list.forEach(System.out::println);

        // TreeSet uses compareTo automatically
        TreeSet<Employee> sorted = new TreeSet<>(list);
        System.out.println("smallest salary: " + sorted.first());
    }

    static class Employee implements Comparable<Employee> {
        final String name;
        final int salary;
        Employee(String name, int salary) { this.name = name; this.salary = salary; }

        @Override
        public int compareTo(Employee o) {
            // Use Integer.compare to avoid overflow vs (this.salary - o.salary)
            return Integer.compare(this.salary, o.salary);
        }

        @Override public String toString() { return name + "=" + salary; }
    }
}