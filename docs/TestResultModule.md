# TestResult Module

This module exists to assist in the facilitation of testbuilds on the server.

## Required Dependencies
- `Vault`: This is how the promotions and demotions occur when passing/failing/starting a testbuild.

## Commands
- `/pass`: Pass a test build.
  - Permission: `greenfieldcore.testresult.pass`
  - Command Help:
    - `/pass <userToPass> <comments>`
      - `<userToPass>`: The user to pass. The user specified MUST have an active test build attempt in order to be passed by this command.
      - `<comments>`: The comments to send to the user. Should be a quoted string, eg "comments"
- `/fail`: Fail a test build. **This will automatically start a new attempt upon issuance, unless the `-final` flag is used.**
  - Permission: `greenfieldcore.testresult.fail`
  - Command Help:
    - `/fail <userToFail> <comments>`
      - `<userToFail>`: The user to fail. The user specified MUST have an active test build attempt in order to be failed by this command.
      - `<comments>`: The comments to send to the user. Should be a quoted string, eg "comments"
  - Command Flags:
    - `-final`: Mark this attempt as the final attempt. This will remove the user from the server whitelist, set them to spectator rank, and kick them from the server after 10 seconds of issuance.
- `/start`: Start a test build.
  - Permission: `greenfieldcore.testresult.start`
  - Command Help:
    - `/start <userToStart>`
      - `<userToStart>`: The user to start. Only a user with the permission `group.spectator` can have a test attempt start.
- `/attempts`: View test build attempts.
  - Permission: `greenfieldcore.testresult.list`
  - Command Help:
    - `/attempts [userToView]` 
      - `[userToView]`: Optional. The user to view the testbuild attempts of. If no user specified, the command will show ALL testbuild attempts.
- `/testinfo`: Sends the player the test information list defined in the configuration file.
    - Permission: `greenfieldcore.testinfo`

## Configuration
 
Configuration file is `testbuild-config.yml`.  
Attempt storage file is `testbuild-storage.yml`. I recommend never modifying this file manually.

An example `testbuild-config.yml`:
```yaml
# The group to move the user to when they start a test build.
testing-group: Testing

# The group to move the user to when they pass a test build.
passing-group: Apprentice

# The group to move the user to when they fail a test build.
failing-group: Spectator

# The test info to send to the user when the `/testinfo` command is run.
test-info:
- Absolutely no hyper-modern buildings.
- Maximum number of floors in a house is 3. (Excluding basement)
- Interior must be completed.
- All codes (/codes, or acceptance message) must be followed. You may reference these
  during your test.
- '@ an administrator in Discord when you complete your test build.'
- You have 30 days and 3 attempts to complete your test build. If you take longer
  than 30 days or fail 3 attempts, you will be remove from the whitelist and will
  need to reapply.
- You may build within the wooden logs. Your house cannot sit on the logs, but your
  fence can.
- You may not ask anyone for help nor advice once the test has begun.
- You are to build an American styled house from any era. (Keep it realistic to what
  you would find in a typical, American, city.
- You may use a google earth/maps reference for your test build.
```

## Example Usage
1. Start to finish, decent testbuild
   * User [NJDaeger] joins server wanting to do testbuild`
   * To start testbuild, staff member should issue `/start NJDaeger`
       - User is moved to the group `Testing`
   * User builds shitty testbuild
   * Staff member does not like their first attempt, issues `/fail NJDaeger "This is why your build was rejected"`
       - A new testbuild is immediately started for the user.
   * Staff member likes their new testbuild a lot and issues `/pass NJDaeger "This testbuild fixed many of the problems with your last testbuild. Good job!"`
       - User is moved to the group `Apprentice`
       - Testbuild is marked as passed.
2. User fails their testbuild
    * User [NJDaeger] joins server wanting to do testbuild`
    * To start testbuild, staff member should issue `/start NJDaeger`
         - User is moved to the group `Testing`
    * User builds shitty testbuild
    * Staff member does not like their first attempt, issues `/fail NJDaeger "This is why your build was rejected"`
         - A new testbuild is immediately started for the user.
    * User does *N* more testbuilds.
    * Staff member does not like their final attempt, issues `/fail NJDaeger "This is why your final build was rejected" flags: -final`
         - User is moved to the group `Spectator`
         - User is removed from the whitelist
         - User is kicked from the server after 10 seconds
