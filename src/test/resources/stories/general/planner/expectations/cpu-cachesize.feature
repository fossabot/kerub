Feature: support for host cpu cache-size expectation

  Scenario: host selection with manufacturer info
    Given hosts:
      | address           | ram  | Cores | Threads | Architecture |  |
      | host1.example.com | 6 GB | 2     | 4       | x86_64       |  |
      | host2.example.com | 6 GB | 2     | 4       | x86_64       |  |
    And host1.example.com manufaturer has 512 KB L1 cache
    And host1.example.com manufaturer has 1024 KB L2 cache
    And host2.example.com manufaturer has NO L1 cache
    And host2.example.com manufaturer has NO L2 cache
    And host host1.example.com is Up
    And host host2.example.com is Up
    And VMs:
      | name | MinRam | MaxRam | CPUs | Architecture |
      | vm1  | 4 GB   | 4 GB   | 2    | x86_64       |
    And VM vm1 requires 512 KB L1 cache
    When VM vm1 is started
    Then VM vm1 gets scheduled on host host1.example.com