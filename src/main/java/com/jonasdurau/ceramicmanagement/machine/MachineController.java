package com.jonasdurau.ceramicmanagement.machine;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jonasdurau.ceramicmanagement.machine.dto.MachineRequestDTO;
import com.jonasdurau.ceramicmanagement.machine.dto.MachineResponseDTO;
import com.jonasdurau.ceramicmanagement.shared.generic.IndependentController;

@RestController
@RequestMapping("/api/machines")
public class MachineController extends IndependentController<MachineResponseDTO, MachineRequestDTO, MachineResponseDTO, Long, MachineService>{
}
