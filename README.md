<!-- Headings -->
# Introduction
Simulator and Diagram Maker Program for Electric Circuits (SDMPEC) is a SPICE-inspired simulator written in Java. The program takes a text file which contains all the circuit nodes and components labeled according to specific rules. By using Nodal Analysis, the program solves the circuit for each time step and sets a voltage value for each node. Our program can simulate circuits including elements such as resistor, capacitor, inductor, voltage source (DC and Sinusoidal), current source (DC and Sinusoidal), and linear dependent sources. Other than simulating, the program is able to draw a circuit based on the input text file.
The program has the following features:
1. DC Operating Point Analysis
1. Transient Analysis
1. Drawing Circuit Diagram
User interface looks like below:
<!-- Image of interface -->
![User Interface](results/Interface.jpg)
---
# Performance Examination
In this section we examine program's performance with different circuit inputs. The input text file consists of elements described in an HSPICE format.
Example:
```
***Input***

```
## Resistors and Current Sources Test
```

```