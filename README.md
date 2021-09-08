<!-- Headings -->
# Introduction
Simulator and Diagram Maker Program for Electric Circuits (SDMPEC) is a SPICE-inspired simulator written in Java. The program takes a text file which contains all the circuit nodes and components labeled according to specific rules. By using Nodal Analysis, the program solves the circuit for each time step and sets a voltage value for each node. Our program can simulate circuits including elements such as resistor, capacitor, inductor, voltage source (DC and Sinusoidal), current source (DC and Sinusoidal), and linear dependent sources. Other than simulating, the program is able to draw a circuit based on the input text file.
The program has the following features:
1. DC Operating Point Analysis
1. Transient Analysis
1. Drawing Circuit Diagram

User interface of the program looks like below:
<!-- Image of interface -->
![User Interface](results/Interface.jpg)
---
# Performance Examination
In this section we examine program's performance with different circuit inputs. The input text file consists of elements described in an HSPICE format and three additional variables; *dv*, *di* and *dt* are three manually-selected input variables which take part in finding node voltages for each time step.  
## Example 1: Resistors and Current Sources
Input text file:
```
***Input***
dv 1m
di 10u
dt 100u
I1 1 0 3m 0 0 0
R1 1 2 4K
R2 2 3 4K
R3 1 3 1K
R4 2 0 1K
R5 3 0 2K
.tran 20m
```
Operating point:
```
      ---------- Time:0.019999973, step:200 ----------
 Node: 0	 voltage[V]:0.000
 Node: 1	 voltage[V]:5.232
 Node: 2	 voltage[V]:1.403
 Node: 3	 voltage[V]:3.189

 Branch: I1	voltage[V]:-5.232, current[mA]:3.0, power[mW]:-15.7
 Branch: R1	voltage[V]:3.829, current[mA]:1.0, power[mW]:3.7
 Branch: R2	voltage[V]:-1.786, current[mA]:-0.4, power[mW]:0.8
 Branch: R3	voltage[V]:2.042, current[mA]:2.0, power[mW]:4.2
 Branch: R4	voltage[V]:1.403, current[mA]:1.4, power[mW]:2.0
 Branch: R5	voltage[V]:3.189, current[mA]:1.6, power[mW]:5.1
```
Drawn circuit:
![Example1-DrawnCircuit](results/Example1-DrawnCircuit.jpg)