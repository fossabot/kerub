Feature: support for not-same-storage expectation

  Scenario: When a not-same-storage virtual disk is allocated on host, allocate storage on another host
    Given hosts:
      | address           | ram  | Cores | Threads | Architecture |  |
      | host1.example.com | 8 GB | 2     | 4       | x86_64       |  |
      | host2.example.com | 8 GB | 2     | 4       | x86_64       |  |
    And VMs:
      | name | MinRam | MaxRam | CPUs | Architecture |
      | vm1  | 4 GB   | 4 GB   | 2    | x86_64       |
      | vm2  | 1 GB   | 1 GB   | 2    | x86_64       |
    And virtual storage devices:
      | name          | size | ro    |
      | vm1-disk      | 2 GB | false |
      | vm2-disk      | 2 GB | false |
    And vm1-disk is attached to vm1
    And vm1-disk is not yet created
    And vm2-disk is attached to vm1
    And vm2-disk is created on host2.example.com
    And host1.example.com is Up
    And host2.example.com is Up
    When vm1 is started
    Then vm1-disk must be allocated on host1.example.com
